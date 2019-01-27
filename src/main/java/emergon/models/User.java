/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emergon.models;

import java.math.BigInteger;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

/**
 *
 * @author anastasios
 */
public class User {
    @Id
    private Integer _id;
    private String fname;
    private String lname;
    private String address;
    private String phone;
    private int upvotes;

    public User() {
    }

    public User(Integer _id, String fname, String lname, String address, String phone, int upvotes) {
        this._id = _id;
        this.fname = fname;
        this.lname = lname;
        this.address = address;
        this.phone = phone;
        this.upvotes = upvotes;
    }

    public Integer getId() {
        return _id;
    }

    public void setId(Integer _id) {
        this._id = _id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    @Override
    public String toString() {
        return "User{" + "_id=" + _id + ", fname=" + fname + ", lname=" + lname + ", address=" + address + ", phone=" + phone + ", upvotes=" + upvotes + '}';
    }
}
