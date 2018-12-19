package com.wfnex.app.sal.service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.owlike.genson.Genson;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class RestClient {
    private static final SSLContext sslContext = disableSslVerification();
    private static final ClientConfig clientConfig = getHttpUrlConnectorConfig();
    
    protected SlaMain slaMain;
    
    private Client client;
    private WebTarget target;
    
    public RestClient(SlaMain slaMain) {
    	this.slaMain = slaMain;
    }
    
    private URI getBaseURI() {
    	String baseURI = "https://" + slaMain.configMgr.getRestSvrIP() + "/rest/v1.0";
    	return UriBuilder.fromUri(baseURI).build();
    }

    protected int init() {
    	System.out.println("RestClient::init");
    	
    	sslConnectDisableCertValidation();
    	
    	return 0;
    }
    
    private static ClientConfig getHttpUrlConnectorConfig() {
        return new ClientConfig().connectorProvider(new HttpUrlConnectorProvider());
    }
    
    private static SSLContext disableSslVerification() {
    	SSLContext sc = null;
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        
        return sc;
    }

    public void sslConnectDisableCertValidation() {
    	System.out.println("sslConnectDisableCertValidation");
    	
        client = ClientBuilder.newBuilder().withConfig(clientConfig)
                .sslContext(sslContext).build();

        // client basic auth demonstration
        client.register(HttpAuthenticationFeature.basic("sonus", "sonus123"));

        final URI BASE_URI = getBaseURI();
        
        System.out.println("Client: GET " + BASE_URI);

        target = client.target(BASE_URI);
    }
    
    public static class FlowStats {
    	String flowId = "-1";
    	String Bandwidth = "0";
    	
		public FlowStats(String flowId, String bandwidth) {
			this.flowId = flowId;
			Bandwidth = bandwidth;
		}

		public String getFlowId() {
			return flowId;
		}

		public String getBandwidth() {
			return Bandwidth;
		}

		public void setFlowId(String flowId) {
			this.flowId = flowId;
		}

		public void setBandwidth(String bandwidth) {
			Bandwidth = bandwidth;
		}

		@Override
		public String toString() {
			return "FlowIdBandwidth [" + (flowId != null ? "flowId=" + flowId + ", " : "")
					+ (Bandwidth != null ? "Bandwidth=" + Bandwidth : "") + "]";
		}
    	
    	
    }
    
    public void queryFlows() {
        final Response response = target.path("flow").request().get(Response.class);

        System.out.println("status: " + response.getStatus());
        //System.out.println(response.readEntity(String.class));
        
        if (response.getStatus() != 200) {
        	return;
        }
        
        InputStream in = response.readEntity(InputStream.class);
        
        // Read from the stream
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
		try {
			jsonNode = mapper.readTree(in);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
        JsonNode items = jsonNode.get("items");
        if (items == null) {
            System.err.println("Invalid argument, no key with name \"items\".");
            return;
        }

        LinkedList <FlowStats> itemList = new LinkedList<FlowStats>();
        
        ArrayNode itemsArray = (ArrayNode) items;
        for (int i = 0; i < itemsArray.size(); ++i) {
        	JsonNode item = itemsArray.get(i);
        	ObjectNode itemObj = (ObjectNode) item;
        	if (null == itemObj) {
        		continue;
        	}
        	
        	JsonNode flowId = itemObj.get("id");
        	JsonNode maxBandwidth = itemObj.get("maximum-bandwidth");
        	
        	itemList.add(new FlowStats(flowId.asText(), maxBandwidth.asText()));
        }
        
        System.out.println("entries in itemList:");
        for (FlowStats entry : itemList) {
        	System.out.println(entry);
        }
        
        try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public LinkedList<String> getFlowIdList() {
    	LinkedList<String> flowIdList = new LinkedList<String>();    	

        final Response response = target.path("flow").request().get(Response.class);
        System.out.println("getFlowIdList, status: " + response.getStatus());
        //System.out.println(response.readEntity(String.class));
        
        if (response.getStatus() != 200) {
        	return null;
        }
        
        InputStream in = response.readEntity(InputStream.class);
        
        // Read from the stream
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
		try {
			jsonNode = mapper.readTree(in);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
        JsonNode items = jsonNode.get("items");
        if (items == null) {
            System.err.println("getFlowIdList, invalid argument, no key with name \"items\".");
            return null;
        }

        ArrayNode itemsArray = (ArrayNode) items;
        for (int i = 0; i < itemsArray.size(); ++i) {
        	JsonNode item = itemsArray.get(i);
        	ObjectNode itemObj = (ObjectNode) item;
        	if (null == itemObj) {
        		continue;
        	}
        	
        	JsonNode flowId = itemObj.get("id");        	
        	flowIdList.add(flowId.asText());
        }
        
        System.out.println("getFlowIdList, entries in flowIdList:");
        for (String entry : flowIdList) {
        	System.out.println(entry);
        }
        
        try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    	return flowIdList;
    }
    
    // Max-bandwidth is in kbps.
    public int getMaxBandWidthByFlowId(String flowId) {
    	int maxBandWidth = -1;
    	
    	System.out.println("getMaxBandWidthByFlowId, flowId = " + flowId);
    	
        final Response response = target.path("flow/" + flowId).request().get(Response.class);
        System.out.println("getMaxBandWidthByFlowId, status: " + response.getStatus());
        //System.out.println(response.readEntity(String.class));
        
        if (response.getStatus() != 200) {
        	return -1;
        }
        
        InputStream in = response.readEntity(InputStream.class);
        
        // Read from the stream
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
		try {
			jsonNode = mapper.readTree(in);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return -1;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return -1;
		}
		
		ObjectNode flowObj = (ObjectNode) jsonNode;
        if (null == flowObj) {
            System.err.println("getMaxBandWidthByFlowId, flowId = " + flowId + " invalid argument, flowObj is null.");
            return -1;
        }
        
        JsonNode maxBandwidthNode = flowObj.get("maximum-bandwidth");
        maxBandWidth = maxBandwidthNode.asInt();
        
        try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	return maxBandWidth;
    }    
    
    // Speed of the flow in bytes per second.
    // curBandWidthArray[0]: flow statistics(forward); curBandWidthArray[1]: flow statistics(reverse)
	public int getCurBandWidthByFlowId(String flowId, int curBandWidthArray[]) {
		System.out.println("getCurBandWidthByFlowId, flowId = " + flowId);
    	
    	//$ curl -k -u sonus:sonus123 -X GET https://103.235.222.60/rest/v1.0/flow-stats/F-LG2WFJ-Port
    	//{"items": [{"error":"Statistics are not available."}, {"error":"Statistics are not available."}]}
        final Response response = target.path("flow-stats/" + flowId).request().get(Response.class);
        System.out.println("getCurBandWidthByFlowId, status: " + response.getStatus());
        //System.out.println(response.readEntity(String.class));
        
        if (response.getStatus() != 200) {
        	return -1;
        }
        
        InputStream in = response.readEntity(InputStream.class);
        
        // Read from the stream
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
		try {
			jsonNode = mapper.readTree(in);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return -1;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return -1;
		}
		
		ObjectNode flowStatsObj = (ObjectNode) jsonNode;
        if (null == flowStatsObj) {
            System.err.println("getCurBandWidthByFlowId, flowId = " + flowId + ", invalid argument, flowStatsObj is null.");
            return -1;
        }
        
        System.out.println("getCurBandWidthByFlowId, flowId = " + flowId + ", dump flowtStatsObj: " + flowStatsObj.toString()); 
        
        JsonNode flowStatsNode = flowStatsObj.get("items");
        if (null == flowStatsNode) {
            System.err.println("getCurBandWidthByFlowId, flowId = " + flowId + ", invalid argument, flowStatsNode is null.");
            return -1;
        }
        ArrayNode flowStats = (ArrayNode) flowStatsNode;
        
        for (int i = 0; i < flowStats.size(); ++i) {
        	JsonNode itemNode = flowStats.get(i);
        	ObjectNode item = (ObjectNode) itemNode;
        	if (null == item) {
                System.err.println("getCurBandWidthByFlowId, flowId = " + flowId + ", i = " + i + ", invalid argument, item is null.");
                return -1;
        	}
        	
        	JsonNode errorNode = item.get("error");
        	if (errorNode != null) {
                System.err.println("getCurBandWidthByFlowId, flowId = " + flowId + ", i = " + i + ": " + item.toString());
                return -1;        		
        	}
        	
            JsonNode curBandwidthNode = item.get("bytes-per-sec");
            if (null == curBandwidthNode) {
                System.err.println("getCurBandWidthByFlowId, flowId = " + flowId + ", i = " + i + ", invalid argument, curBandwidthNode is null.");
                return -1;
            }
            int bandwidth = curBandwidthNode.asInt();
 
            JsonNode directionNode = item.get("direction");
            if (null == directionNode) {
                System.err.println("getCurBandWidthByFlowId, flowId = " + flowId + ", i = " + i + ", invalid argument, directionNode is null.");
                return -1;
            }
            String direction = directionNode.asText();
            if (direction.equals("forward")) {
            	curBandWidthArray[0] = bandwidth;
            } else if (direction.equals("reverse")) {
            	curBandWidthArray[1] = bandwidth;
            } else {
                System.err.println("getCurBandWidthByFlowId, flowId = " + flowId + ", i = " + i + ", invalid argument, unkown direction("
                				   + direction + ").");
                return -1;
            }
        }
        
        try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return 0;
	}
	
    private class Flow {
    	private String id;
    	private List<Object> destinations = new LinkedList<Object>();
    	private String source;
    	private String flowDirection;
    	private String description;
    	int maximumBandwidth;
    	int maximumHopCount;

		public Flow(String flowId) {
			id = flowId;
	    	source = "No";
	    	flowDirection = "No";
	    	description = "No";
	    	maximumBandwidth = -1;
	    	maximumHopCount = -1;
		}
		
		public Flow(String source, String flowDirection, String description, int maximumBandwidth,
				int maximumHopCount) {
			this.source = source;
			this.flowDirection = flowDirection;
			this.description = description;
			this.maximumBandwidth = maximumBandwidth;
			this.maximumHopCount = maximumHopCount;
		}
		
		public String getId() {
			return id;
		}
		
		/*public synchronized void setDestinationsArray(JSONArray array) {       
	        for (String item : destinations) {
	        	array.put(item);
	        }	        
		}*/

		public synchronized List<Object> getDestinations() {
			return destinations;
		}

		public synchronized void addDestination(String dst) {
			destinations.add(dst);
		}

		public String getSource() {
			return source;
		}

		public String getFlowDirection() {
			return flowDirection;
		}

		public String getDescription() {
			return description;
		}

		public int getMaximumBandwidth() {
			return maximumBandwidth;
		}

		public int getMaximumHopCount() {
			return maximumHopCount;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public void setFlowDirection(String flowDirection) {
			this.flowDirection = flowDirection;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setMaximumBandwidth(int maximumBandwidth) {
			this.maximumBandwidth = maximumBandwidth;
		}

		public void setMaximumHopCount(int maximumHopCount) {
			this.maximumHopCount = maximumHopCount;
		}

		@Override
		public String toString() {
			return "Flow [" + (destinations != null ? "destinations=" + destinations + ", " : "")
					+ (source != null ? "source=" + source + ", " : "")
					+ (flowDirection != null ? "flowDirection=" + flowDirection + ", " : "")
					+ (description != null ? "description=" + description + ", " : "") + "maximumBandwidth="
					+ maximumBandwidth + ", maximumHopCount=" + maximumHopCount + "]";
		}
    }
    
    public int updateFlowMaxBandWidth(String flowId, int maxBandWidth) {
    	Flow flow = getFlowById(flowId);
    	if (null == flow) {
    		System.err.println("updateFlowMaxBandWidth, failed to get flow with id " + flowId);
    		return -1;
    	}
    	
    	flow.setMaximumBandwidth(maxBandWidth);
    	int retVal = addFlow(flow);
    	if (retVal != 0) {
    		System.err.println("updateFlowMaxBandWidth, failed to add flow with id " + flowId);
    		return retVal;
    	}
    	
    	return 0;
    }
    
    private Flow getFlowById(String flowId) {
        final Response response = target.path("flow/" + flowId).request().get(Response.class);
        
        System.out.println("getFlowById, flowId = " + flowId + " status: " + response.getStatus());
        //System.out.println(response.readEntity(String.class));
        
        if (response.getStatus() != 200) {
        	return null;
        }
        
        InputStream in = response.readEntity(InputStream.class);
        
        Flow flow = new Flow(flowId);
        
        // Read from the stream
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
		try {
			jsonNode = mapper.readTree(in);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		ObjectNode flowObj = (ObjectNode) jsonNode;
        if (null == flowObj) {
            System.err.println("getFlowById, flowId = " + flowId + " invalid argument, flowObj is null.");
            return null;
        }
        
        JsonNode source = flowObj.get("source");
        flow.setSource(source.asText());
        
        JsonNode destinations = flowObj.get("destinations");
        ArrayNode dstArray = (ArrayNode) destinations;
        for (int i = 0; i < dstArray.size(); ++i) {
        	JsonNode item = dstArray.get(i);
        	System.out.println("getFlowById, flowId = " + flowId + "dstArray: " + item.asText());
        	flow.addDestination(item.asText());
        }
        
        JsonNode direction = flowObj.get("flow-direction");
        flow.setFlowDirection(direction.asText());
        
        JsonNode description = flowObj.get("description");
        System.out.println("getFlowById, flowId = " + flowId + ", description.asText() = " + description.asText());
        flow.setDescription(description.asText());
        
        JsonNode maxBandwidth = flowObj.get("maximum-bandwidth");
        flow.setMaximumBandwidth(maxBandwidth.asInt());
        
        JsonNode maxHopCount = flowObj.get("maximum-hop-count");
        System.out.println("getFlowById, flowId = " + flowId + ", maxHopCount.asInt() = " + maxHopCount.asInt());
        flow.setMaximumHopCount(maxHopCount.asInt());
        
        try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return flow;
    }
    
    private int delFlowById(String flowId) {
        final Response response = target.path("flow/" + flowId).request().delete();
        
        System.out.println("status: " + response.getStatus());

        if (response.getStatus() != 200) {
        	return -1;
        }
        
        return 0;
    }
    
    private int addFlow(Flow flow) {
    	Genson genson = new Genson();
    	
    	// String generated by genson.serialize needed to be processed further.
    	//{"description":"null","destinations":["WFJ_test"],"flowDirection":"uni","id":"F-LG2WFJ-test","maximumBandwidth":70,"maximumHopCount":0,"source":"LG-test"}
    	String newFlow = genson.serialize(flow);
    	
    	newFlow = newFlow.replace("\"null\"", "null");
    	newFlow = newFlow.replace("flowDirection", "flow-direction");
    	newFlow = newFlow.replace("maximumBandwidth", "maximum-bandwidth");
    	newFlow = newFlow.replace("maximumHopCount", "maximum-hop-count");
    	
        final Response response = target.path("flow/" + flow.getId()).request().put(Entity.entity(newFlow, "application/json"));
        
        StatusType status = response.getStatusInfo();
        System.out.println("addFlow, flowId = " + flow.getId() + ", status: " + status.getStatusCode() + ", reason = " + status.getReasonPhrase());
        System.out.println("addFlow, flowId = " + flow.getId() + ", status: " + response.getStatus());
        
        if (response.getStatus() != 200) {
        	return -1;
        }
                
    	return 0;
    }
    
}