/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emergon.MongoRestApi;


import emergon.models.Incident;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author anastasios
 */
@Repository
public interface IncidentRepository extends MongoRepository<Incident, Integer> {
    Incident findBy_id(Integer _id);
    
    //List<User> findByUpvotes(int upvotes);
}
