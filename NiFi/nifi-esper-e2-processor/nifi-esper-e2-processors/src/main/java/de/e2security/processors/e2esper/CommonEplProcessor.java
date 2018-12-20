package de.e2security.processors.e2esper;

import static de.e2security.processors.e2esper.utilities.EsperProcessorLogger.failure;
import static de.e2security.processors.e2esper.utilities.EsperProcessorLogger.success;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;

import de.e2security.nifi.controller.esper.EsperService;
import de.e2security.processors.e2esper.utilities.UnmatchedEventListener;
import de.e2security.processors.e2esper.utilities.SuccessedEventListener;
import de.e2security.processors.e2esper.utilities.SupportUtility;

@Tags({"EsperProcessor"})
@CapabilityDescription("Processing events based on esper engine rules)")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class CommonEplProcessor extends AbstractProcessor {
	
	public static final PropertyDescriptor ESPER_ENGINE = new PropertyDescriptor.Builder().name("EsperEngine")
			.displayName("EsperEngineService")
			.description("esper main engine")
			.required(true)
			.identifiesControllerService(EsperService.class)
			.build();
	
	public static final PropertyDescriptor EPL_STATEMENT = new PropertyDescriptor.Builder()
			.name("EplStatement")
			.displayName("EplStatement")
			.description("epl statement")
			.required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.build();
	
	public static final PropertyDescriptor INBOUND_EVENT_NAME = new PropertyDescriptor.Builder()
			.name("InboundEventName")
			.displayName("InboundEventName")
			.description("name of incoming event against which epl statement should be evaluated")
			.required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.build();

	public static final PropertyDescriptor EVENT_SCHEMA = new PropertyDescriptor.Builder()
			.name("InputEventSchema")
			.displayName("InputEventSchema")
			.description("define schema with EPL as string. In case of complex event schema declaration divide multiple strings with '|'")
			.required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.build();

	public static final Relationship SUCCEEDED_EVENT = new Relationship.Builder()
			.name("SucceededEvent")
			.description("esper event matched epl statement")
			.build();

	public static final Relationship UNMATCHED_EVENT = new Relationship.Builder()
			.name("FailedEvent")
			.description("esper event unmatched epl statement")
			.build();
	
	private List<PropertyDescriptor> descriptors;

	private Set<Relationship> relationships;

	@Override
	protected void init(final ProcessorInitializationContext context) {
		final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
		descriptors.add(EVENT_SCHEMA);
		descriptors.add(EPL_STATEMENT);
		descriptors.add(INBOUND_EVENT_NAME);
		descriptors.add(ESPER_ENGINE);
		this.descriptors = Collections.unmodifiableList(descriptors);

		final Set<Relationship> relationships = new HashSet<Relationship>();
		relationships.add(SUCCEEDED_EVENT);
		relationships.add(UNMATCHED_EVENT);
		this.relationships = Collections.unmodifiableSet(relationships);
	}

	@Override
	public Set<Relationship> getRelationships() {
		return this.relationships;
	}

	@Override
	public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return descriptors;
	}

	@OnScheduled
	public void onScheduled(final ProcessContext context) {	}
	
	@Override
	public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
		FlowFile flowFileS = session.get(); //for SUCCEEDED_EVENT REL
		if ( flowFileS == null ) { return;} //for UNMATCHED_EVENT REL
//		FlowFile flowFileF = session.clone(flowFileS);
		//load esper engine from the controller
		EsperService esperService = context.getProperty(ESPER_ENGINE).asControllerService(EsperService.class);
		EPServiceProvider esperEngine = esperService.execute();
		EPRuntime runtime = esperEngine.getEPRuntime();
		EPAdministrator admin = esperEngine.getEPAdministrator();
		
		// parse each epl statement from array of strings defined in EplStatement
		SupportUtility.parseMultipleEventSchema(context.getProperty(EVENT_SCHEMA).getValue(),admin,getLogger());
		final String _EPL_STATEMENT = context.getProperty(EPL_STATEMENT).getValue();
		EPStatement eplIn = admin.createEPL(_EPL_STATEMENT); //do not try to catch error; this is a runtime critical one
		getLogger().debug(success("IMPLEMENTED EPL STMT", _EPL_STATEMENT));
		//processing incoming nifi events
		SuccessedEventListener sel = new SuccessedEventListener(getLogger());
		eplIn.addListener(sel);
		UnmatchedEventListener fel = new UnmatchedEventListener(getLogger());
		runtime.setUnmatchedListener(fel);
		final String _INBOUND_EVENT_NAME = context.getProperty(INBOUND_EVENT_NAME).getValue(); 
		session.read(flowFileS, (inputStream) -> {
			String eventMapAsString = "";
			try {
				String eventJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
				//parsing inputStream as JSON objects
				Map<String,Object> eventMap = SupportUtility.transformEventToMap(eventJson);
				eventMapAsString = eventMap.entrySet().toString();
				runtime.sendEvent(eventMap, _INBOUND_EVENT_NAME);
				getLogger().debug(success("PROCESSED EVENT AS MAP", eventMapAsString));
			} catch (EPException epx) {
				getLogger().error(epx.getMessage());
				epx.printStackTrace();
				getLogger().debug(failure("PROCESSING EVENT",eventMapAsString));
			}
		});
		if (sel.getProcessedEvent() != null) {
			session.write(flowFileS, (outStream) -> {
				outStream.write(sel.getProcessedEvent().getBytes()); 
			});
			session.transfer(flowFileS, SUCCEEDED_EVENT);
		} else if (fel.getUnmatchedEvent() != null) {
			session.write(flowFileS, (outStream) -> {
				outStream.write(fel.getUnmatchedEvent().getBytes()); 
			});
			session.transfer(flowFileS, UNMATCHED_EVENT);
		} else {
			getLogger().error("NEITHER SUCCEEDED NOR UNMATCHED EVENT PROCESSED TO FLOW FILE");
		}
		session.commit();
	}
}
