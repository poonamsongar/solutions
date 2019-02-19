import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class IntelApiCall {


        //Create Object category which has list of fields and name of the category
        public static class Category {

            String name;
            //comma separated list of field names
            String listOfFields;

            //For this cat get an array of field names
            String[] fieldsArr;

            Category(String name1, String list_of_fields1) {
                name = name1;
                listOfFields = list_of_fields1;
                fieldsArr = listOfFields.split(",", -1);
            }




        }



        //Make connection call for a Category
        public static JSONObject makeGetCall( Category cat) {

            JSONObject outputJSON = new JSONObject();

            try {

                String inject_category_name = cat.name;
                String inject_fields = cat.listOfFields;
                URL url = new URL("https://odata.intel.com/API/v1_0/Products/" + inject_category_name + "()?&$select=" + inject_fields + "&$format=json");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                //Build a String from the Get Response
                StringBuilder sb = new StringBuilder();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                conn.disconnect();


                try {
                    //Build JSON object from the string builder
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonStringResponse = (JSONObject) jsonParser.parse(String.valueOf(sb));
                    JSONArray jsonGetProdArr = (JSONArray) jsonStringResponse.get("d");
                    Iterator<String> prodIterator = jsonGetProdArr.iterator();
                    int count_cat = 0;

                    JSONArray catArray = new JSONArray();

                    while (prodIterator.hasNext()) {
                        count_cat++;
                        JSONObject prodLineJSONObject = (JSONObject) jsonParser.parse(String.valueOf(prodIterator.next()));
                        JSONObject catObject = new JSONObject();
                        for (String str : cat.fieldsArr) {
                            catObject.put(String.valueOf(str) , prodLineJSONObject.get(String.valueOf(str)));
                        }
                        catArray.add(catObject);

                    }


                    outputJSON.put("data", catArray);
                    outputJSON.put("totals" , count_cat);

                }

                catch (ParseException e){ e.printStackTrace(); }



            } catch (MalformedURLException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            }


            return outputJSON;

        }



        public static void main(String[] args) {


            //Make 2 categories
            ArrayList<Category> listOfCats = new ArrayList<Category>();

            Category cat1 =  new Category("Processors", "ClockSpeedMhz,MarketSegment,ProductName,LaunchDate");
            Category cat2 =  new Category("EthernetAdapters", "BornOnDate,MarketSegment,ProductName,Link");
            listOfCats.add(cat1);
            listOfCats.add(cat2);

            String workingDir = System.getProperty("user.dir");
            System.out.println("Current working directory : " + workingDir);

            //Create Output File
            long unixTime = System.currentTimeMillis();
            File output_file = new File("output_"+unixTime+".json");

            // Store current System.out before assigning a new value
            PrintStream console = System.out;

            try {
                output_file.createNewFile(); // if file already exists will do nothing
                PrintStream out = new PrintStream(new FileOutputStream(output_file, true), true);
                System.setOut(out);
            }
            catch(IOException e) {
                e.printStackTrace();
            }

            //Do this for each Category
            JSONObject mainJSON = new JSONObject();
            JSONObject totalsJSON = new JSONObject();
            JSONObject dataJSON = new JSONObject();
            for (Category cat : listOfCats ) {
                JSONObject catJSON = makeGetCall(cat);
                int tot =  Integer.parseInt( catJSON.get("totals").toString());
                JSONArray data = (JSONArray) catJSON.get("data");
                totalsJSON.put(cat.name,tot);
                dataJSON.put(cat.name,data);
            }


            mainJSON.put("data",dataJSON);
            mainJSON.put("totals",totalsJSON);

            //Write to file
            System.out.println(mainJSON.toJSONString());


            //reset print out to console
            System.setOut(console);



            //Make Post Request
            StringEntity entity = new StringEntity(mainJSON.toJSONString(),
                    ContentType.APPLICATION_JSON);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost("https://postman-echo.com/post");
            request.setEntity(entity);

            try {HttpResponse response = httpClient.execute(request);
            System.out.println(response.getStatusLine().getStatusCode());}
            catch(Exception e) { e.printStackTrace();}

            System.out.println("done");









        }

    }






