/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emergon.MongoRestApi;

//import emergon.repositories.UserRepository;
import emergon.models.User;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
//import org.springframework.data.mongodb.repository.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author anastasios
 */
@RestController //tells Spring that this class will be requested by URL and will return data to the requester
@RequestMapping("/user") //specifies the base URL that the controller will be handling, so any request to the host starting with “/user” will be directed to this controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    
    @Autowired //creates an instance of the UserRepository object that will allow us to access and modify the user database.
    private UserRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    // takes any GET requests to the host with a URL of /user/ and maps them to the getAllUsers() method, which requests all documents from the user collection.
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List getAllUsers() {
        return repository.findAll();
    }

    //takes any GET requests to the host with a URL of /user/ followed by an ObjectId and maps them to the getUserById() method. This searches the user collection for the document with an _id field equal to the ObjectId in the URL.
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User getUserById(@PathVariable("id") Integer id) {
        return repository.findBy_id(id);
    }

    //expects a request body (in JSON format) with each of the fields that a User object contains (fname, lname, address, phone, upvotes). The ID in the request URL is the _id of the document to be modified.
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void modifyUserById(@PathVariable("id") Integer id, @Valid @RequestBody User user) {
        user.setId(id);
        repository.save(user);
    }

//    @RequestMapping(value = "/upvotes", method = RequestMethod.GET)
//    public List getByVotes() {
//        Query query = new Query();
//        query.addCriteria(Criteria.where("upvotes").is(199));
//        List<User> users = mongoTemplate.find(query, User.class);//.find(query, User.class);
//        return users;
//    }

    //http://localhost:8080/user/mostActiveCitizens
    //query 8 Fifty most active citizens, with regard to the total number of upvotes
    @RequestMapping(value = "/mostActiveCitizens", method = RequestMethod.GET)
    public List<DBObject> getMostActiveCitizens() {
        Aggregation agg = newAggregation(
                sort(Sort.Direction.DESC, "upvotes"),
                limit(50)
        );
        AggregationResults<DBObject> groupResults
                = mongoTemplate.aggregate(agg, "user", DBObject.class);
        List<DBObject> result = groupResults.getMappedResults();
        return result;
    }

    //POST 'http://localhost:8080/user/'
    //With body : { "_id" : 22, "fname" : "Tasos", "lname" : "Lelakis", "address" : "Souniou 10", "phone" : "(693) 238-6432" }
    //and header : 'Content-Type: application/json'
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public BasicDBObject createUser(@RequestBody BasicDBObject user) {
        //user.setId(ObjectId.get());
        //repository.save(user);
        mongoTemplate.insert(user,"user");
        return user;
    }
    
//    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
//    public void deleteUser(@PathVariable("id") Integer id) {
//        repository.delete(repository.findBy_id(id));
//    }
}
