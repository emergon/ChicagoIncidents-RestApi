/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emergon.MongoRestApi;

import emergon.models.Incident;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregationOptions;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Divide;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Subtract;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author anastasios
 */
@RestController
@RequestMapping("/incident")
public class IncidentController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IncidentController.class);
    
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired //creates an instance of the IncidentRepository object that will allow us to access and modify the user database.
    private IncidentRepository repository;
    
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List getAllIncidents() {
        return mongoTemplate.findAll(DBObject.class, "incitest");
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Incident getIncidentById(@PathVariable("id") Integer id) {
        return repository.findBy_id(id);
    }
    

    //http://localhost:8080/incident/totalRequestsPerTypeFrom/2011-05-10/to/2014-06-10
    //query 1
    @RequestMapping(value = "/totalRequestsPerTypeFrom/{startDate}/to/{endDate}", method = RequestMethod.GET)
    public List<DBObject> getTotalRequestsPerType(@PathVariable String startDate, @PathVariable String endDate) {
//        Cond condOperation = ConditionalOperators.when(Criteria.where("type").is(1))
//                                    .then("vehicle")
//                                    .otherwise("light");
        Aggregation agg = newAggregation(
                match(Criteria.where("creationdate").gte(getDate(startDate)).lte(getDate(endDate))),
                group("type").count().as("total"),
                //project("total").and("hosting").previousOperation(),
                sort(Sort.Direction.DESC, "total")//,
        //project().and(condOperation)

        );
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "incident", DBObject.class);
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }

    //http://localhost:8080/incident/totalRequestsPerDayByType/abandoned_vehicle/From/2011-05-10/to/2014-06-10
    //query 2
    @RequestMapping(value = "/totalRequestsPerDayByType/{type}/From/{startDate}/to/{endDate}", method = RequestMethod.GET)
    public List<DBObject> getTotalRequestsPerDayByType(@PathVariable String type, @PathVariable String startDate, @PathVariable String endDate) {
        ProjectionOperation projectToMatchModel = project()
                .andExpression("_id").as("date")
                .andExpression("total").as("Number of Times").andExclude("_id");
        Aggregation agg = newAggregation(
                match(Criteria.where("type").is(type).and("creationdate").gte(getDate(startDate)).lte(getDate(endDate))),//.and("completion").lte(getDate(endDate))),
                group("creationdate").count().as("total"),
                sort(Sort.Direction.ASC, "_id"),
                projectToMatchModel//,
        //project("creation").and("total").previousOperation()
        );
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "incident", DBObject.class);
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }

    //http://localhost:8080/incident/mostCommonRequestsPerZipOn/2016-04-01
    //query 3 most common service requests per zipcode for a specific day
    @RequestMapping(value = "/mostCommonRequestsPerZipOn/{date}", method = RequestMethod.GET)
    public List<DBObject> getMostCommonRequestsPerZipByDate(@PathVariable String date) {
        Aggregation agg = newAggregation(
                match(Criteria.where("creationdate").is(getDate(date))),
                group("zip", "type").count().as("numOfCount"),
                sort(Sort.Direction.ASC, "zip").and(Sort.Direction.DESC, "numOfCount"),
                group("_id.zip").push(new BasicDBObject("type", "$_id.type").append("total_requests", "$numOfCount")).as("types_frequencies"),
                //project("_id").and("types_frequencies").slice(3).as("top_3_types")
                project().andExpression("_id").as("Zipcode").and("types_frequencies").slice(3).as("top_3_types").andExclude("_id")
        );
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "incident", DBObject.class);
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }

    //http://localhost:8080/incident/leastCommonWardByType/abandoned_vehicle
    //https://www.baeldung.com/spring-data-mongodb-projections-aggregations
    //query 4 three least common wards with regards to a given service request type
    @RequestMapping(value = "/leastCommonWardByType/{type}", method = RequestMethod.GET)
    public List<DBObject> getLeastCommonWardByType(@PathVariable String type) {
        ProjectionOperation projectToMatchModel = project()
                .andExpression("_id").as("Ward")
                .andExpression("count").as("Number of Times").andExclude("_id");
        Aggregation agg = newAggregation(
                match(Criteria.where("type").is(type).and("ward").exists(true)),
                group("ward").count().as("count"),
                sort(Sort.Direction.ASC, "count"),
                limit(3), projectToMatchModel
        //project("count").and("_id").as("ward").andInclude("count").andExclude("_id")
        );
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "incident", DBObject.class);
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }

    //http://localhost:8080/incident/avgCompletionTimePerTypeFrom/2017-04-01/to/2017-04-28
    //query 5 Average completion time per service request for a specific date range
    @RequestMapping(value = "/avgCompletionTimePerTypeFrom/{startDate}/to/{endDate}", method = RequestMethod.GET)
    public List<DBObject> getAvgCompletionTimePerTypeByRange(@PathVariable String startDate, @PathVariable String endDate) {
        Aggregation agg = newAggregation(
                match(Criteria.where("creationdate").gte(getDate(startDate)).lte(getDate(endDate))),
                //project("type").and("completion").minus("creation").divide(1000*60*60*24).as("avgTime"),
                group("type").count().as("count")
                        .avg(Divide.valueOf(Subtract.valueOf("completiondate").subtract("creationdate")).divideBy(1000 * 60 * 60 * 24)).as("avgTime"),
                project().andExpression("_id").as("Type").and("avgTime").as("Average Time in Days").andExclude("_id")
        );
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "incident", DBObject.class);
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }
    
    //http://localhost:8080/incident/mostCommonServiceRequestByLatMin/41.90/longMin/-87.75/latMax/41.94/longMax/-87.72/On/2012-07-03
    //query 6 Most common service request in a specified bounding box for a specific day
    @RequestMapping(value = "/mostCommonServiceRequestByLatMin/{latMin}/longMin/{longMin}/latMax/{latMax}/longMax/{longMax}/On/{hmnia}", method = RequestMethod.GET)
    public List<DBObject> getMostCommonServiceRequestByLocationDate(@PathVariable String hmnia, @PathVariable Double latMin, @PathVariable Double longMin, @PathVariable Double latMax, @PathVariable Double longMax) {
        Aggregation agg = newAggregation(
                match(Criteria.where("creationdate").is(getDate(hmnia))
                        .and("latitude").gte(latMin).lte(latMax)
                        .and("longitude").gte(longMin).lte(longMax)),
                group("type").count().as("count"),
                sort(Sort.Direction.DESC, "count"),
                limit(1)
//                        .and("latitude").gte(41.90).lte(41.94)
//                        .and("longitude").gte(-87.75).lte(-87.72))
        );
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "incident", DBObject.class);
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }

    //http://localhost:8080/incident/mostUpvotedIncidentsByDate/2012-05-10
    //query 7 Fifty most upvoted service requests for a specific day
    @RequestMapping(value = "/mostUpvotedIncidentsByDate/{hmnia}", method = RequestMethod.GET)
    public List<DBObject> getMostUpvotedIncidentsByDate(@PathVariable String hmnia) {
        Aggregation agg = newAggregation(
                match(Criteria.where("creationdate").is(getDate(hmnia))),
                sort(Sort.Direction.DESC, "upvotes"),
                limit(50)
        );
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "incident", DBObject.class);
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }

    //http://localhost:8080/incident/topCitizensForMostWards
    //query 9 Top fifty citizens, with regard to the total number of wards for which they have upvoted an incident
    @RequestMapping(value = "/topCitizensForMostWards", method = RequestMethod.GET)
    public List<DBObject> getTopCitizensForMostWards() {
        Aggregation agg = newAggregation(
                match(Criteria.where("ward").exists(true)),
                unwind("user"),
                group("user", "ward").count().as("votesInEachWard"),
                group("_id.user").count().as("differentWards"),
                sort(Sort.Direction.DESC, "differentWards").and(Sort.Direction.ASC, "_id"),
                limit(50)
        ).withOptions(newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "incident", DBObject.class);
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }

    //http://localhost:8080/incident/incidentsWithSamePhoneDifferentCitizen
    //query 10 Incident ids for which the same telephone number has been used for more than one names.
    @RequestMapping(value = "/incidentsWithSamePhoneDifferentCitizen", method = RequestMethod.GET)
    public List<DBObject> getIncidentsWithSamePhoneDifferentCitizen() {
        Aggregation agg = newAggregation(
                //match(Criteria.where("user.phone").is("(347) 214-3757")),
                unwind("user"),
                //match(Criteria.where("user.phone").is("(347) 214-3757")),
                group("_id", "user.phone", "user.fname", "user.lname"),//.count().as("count"),
                sort(Sort.Direction.ASC, "_id"),
                group("phone", "_id._id").count().as("# Of Citizens").push(new BasicDBObject("fname", "$_id.fname").append("lname", "$_id.lname")).as("Citizen Data"),
                sort(Sort.Direction.ASC, "_id"),
                match(Criteria.where("# Of Citizens").gte(2))
        ).withOptions(newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "incident", DBObject.class);//754-324-1448
        //        = mongoTemplate.aggregate(agg, "inci55", DBObject.class);//(347) 214-3757
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }

    //http://localhost:8080/incident/wardsByCitizen/Sydney/Veum
    //query 11 Wards in which a given name has casted a vote for an incident taking place in it
    @RequestMapping(value = "/wardsByCitizen/{fname}/{lname}", method = RequestMethod.GET)
    public List<DBObject> wardsByCitizen(@PathVariable String fname, @PathVariable String lname) {
        Aggregation agg = newAggregation(
                match(Criteria.where("ward").exists(true).and("user.fname").is(fname).and("user.lname").is(lname)),
                project("ward").andExclude("_id"),
                group("ward").first("ward").as("Ward"),
                sort(Sort.Direction.ASC, "_id"),
                project("Ward").andExclude("_id")
        );
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "incident", DBObject.class);
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }
    
    //POST 'http://localhost:8080/incident/'
    //With body : { "_id" : 4500000, "creationdate" : "2019-01-26", "status" : 1, "completiondate" : "2019-01-27", "requestnum" : "19-00000001", "type" : "abandoned_vehicle", "address" : "katexaki 45", "zip" : "60632", "x" : 1149119.58230237, "y" : 1874073.4099702, "ward" : 20, "policedistrict" : 10, "commarea" : 57, "latitude" : 41.8097353374291, "longitude" : -87.7283998293472, "plate" : "IZP-6186", "color" : "Grey", "upvotes" : 0 }
    //and header : 'Content-Type: application/json'
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public BasicDBObject createIncident(@RequestBody BasicDBObject incident) {
        String creationDate = incident.getString("creationdate");
        String completionDate = incident.getString("completiondate");
        incident.replace("creationdate", getDate(creationDate));
        incident.replace("completiondate", getDate(completionDate));
        mongoTemplate.insert(incident,"incident");
        return incident;
    }
    
    //http://localhost:8080/incident/4500000     this is my incident
    //With body : { "fname" : "Tasos", "lname" : "Lelakis", "phone" : "754-324-1448"}
    //and header : 'Content-Type: application/json'
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<String> updateIncident(@PathVariable Integer id, @RequestBody BasicDBObject user) {
        Query query = new Query();
        int incidentId = id;
        //Integer incidentId= (Integer)incident.get("incident");
        String fname = user.getString("fname");
        String lname = user.getString("lname");
        String phone = user.getString("phone");
        LOGGER.info("******Incident:"+incidentId+ " will be upvoted with user: [fname:"+fname+", lname:"+lname+", phone:"+phone+"+]******");
        query.addCriteria(Criteria.where("_id").is(id).and("user.fname").is(fname).and("user.lname").is(lname).and("user.phone").is(phone));
        Incident incident = mongoTemplate.findOne(query, Incident.class, "incident");
        LOGGER.info("******************Incident is "+ incident);
        if(incident == null){
            Update update = new Update();
            update.inc("upvotes", 1);
            update.push("user", new BasicDBObject("fname", fname).append("lname", lname).append("phone", phone));
            query = new Query(Criteria.where("_id").is(id));
            mongoTemplate.updateFirst(query,update,"incident");
            updateUserVote(fname, lname, phone);//update User collection
            return ResponseEntity.ok("Document Was Successfully Updated");
        }else{
            return ResponseEntity.ok("Incident already upvoted. Do not cheat!!!");
        }
    }

    public static Date getDate(String dateStr) {
        String DB_FORMAT_DATETIME = "yyyy-MM-ddHH:mm:ss.SSSZ";
        dateStr = dateStr + "00:00:00.000+0000";
        DateFormat formatter = new SimpleDateFormat(DB_FORMAT_DATETIME);
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
    //icrease upvote of user when upvoting an incident
    public void updateUserVote(String fname, String lname, String phone){
        Query query = new Query();
        query.addCriteria(Criteria.where("fname").is(fname).and("lname").is(lname).and("phone").is(phone));
        Update update = new Update();
        update.inc("upvotes", 1);
        mongoTemplate.updateFirst(query,update,"user");
    }

}
