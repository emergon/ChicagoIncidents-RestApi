/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emergon.MongoRestApi;

import emergon.models.User;
import java.math.BigInteger;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author anastasios
 */
@Repository
public interface UserRepository extends MongoRepository<User, Integer> {
    User findBy_id(Integer _id);
    
    //List<User> findByUpvotes(int upvotes);
}
