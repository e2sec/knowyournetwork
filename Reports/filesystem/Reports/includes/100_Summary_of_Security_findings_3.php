<?php

/*
    Copyright (C) 2017 e-ito Technology Services GmbH
    e-mail: info@e-ito.de

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

$json = '{
  "query": {
    "bool": {
      "must": [
        {
            "match": {
              "ICMPType": {
                "query": 8,
                "type": "phrase"
              }
            }
        },
        {
            "match": {
              "Protocol": {
                "query": 1,
                "type": "phrase"
              }
            }
        },
        {
          "range": {
            "@timestamp": {
              "gte": "' . $startdate . '",
              "lte": "' . $enddate . '"
            }
          }
        }
      ]
    }
  },
  "size": 0,
  "aggs": {
    "ping": {
      "terms": {
        "field": "SourceAddress",
        "size": 20,
        "order": {
          "destip": "desc"
        }
      },
      "aggs": {
        "destip": {
          "cardinality": {
            "field": "DestinationAddress",
            "precision_threshold": 10000
          }
        },
        "ipsum_filter": {
          "bucket_selector": {
            "buckets_path": {
              "m_destip": "destip"
            },
            "script": "params.m_destip > ' . $maxdestip_ping . '"
          }
        }
      } 
    }
  }
}';

$params = array(
    'index' => 'e2nw-netflow-*',
     'body' => $json
);

$response = $elastic->search($params);
$file = fopen("csv/100_Summary_of_Security_findings_d.csv", "w");

// Check if Elasticsearch was able to provide any result
  if (isset($response['aggregations']['ping']['buckets'][0]['key'])) {
    // Check if more than 1 element is within the array
    if (isset($response['aggregations']['ping']['buckets'][1]['key'])) {
       // copy 1st array element to variable and remove it from array to board foreach loop
       // without doublicate entries
       $txt = $response['aggregations']['ping']['buckets'][0]['key'];
       unset($response['aggregations']['ping']['buckets'][0]);
       foreach ($response['aggregations']['ping']['buckets'] as $data) {
             $txt .= ', ' . $data['key'];
       }
       $txt .= PHP_EOL;
    } else {
       // copy single array element to variable
       $txt = $response['aggregations']['ping']['buckets'][0]['key'] . PHP_EOL;;
    }
  } else {
     $txt = "No IP address matched!". PHP_EOL;
  }

fwrite($file, $txt);
fclose($file);

?>
