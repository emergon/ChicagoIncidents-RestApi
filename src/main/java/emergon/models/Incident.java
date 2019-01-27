/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emergon.models;

import java.util.Date;

/**
 *
 * @author anastasios
 */
public class Incident {
    private Integer _id;
    private Date creationdate;
    private Integer status;
    private Date completiondate;
    private String requestnum;
    private String type;
    private String address;
    private String zip;
    private double x;
    private double y;
    private int ward;
    private int policedistrict;
    private int commarea;
    private double latitude;
    private double longitude;
    private int upvotes;

    public Incident() {
    }

    public Integer getId() {
        return _id;
    }

    public void setId(Integer _id) {
        this._id = _id;
    }

    public Date getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(Date creationdate) {
        this.creationdate = creationdate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCompletiondate() {
        return completiondate;
    }

    public void setCompletiondate(Date completiondate) {
        this.completiondate = completiondate;
    }

    public String getRequestnum() {
        return requestnum;
    }

    public void setRequestnum(String requestnum) {
        this.requestnum = requestnum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getWard() {
        return ward;
    }

    public void setWard(int ward) {
        this.ward = ward;
    }

    public int getPolicedistrict() {
        return policedistrict;
    }

    public void setPolicedistrict(int policedistrict) {
        this.policedistrict = policedistrict;
    }

    public int getCommarea() {
        return commarea;
    }

    public void setCommarea(int commarea) {
        this.commarea = commarea;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    @Override
    public String toString() {
        return "Incident{" + "_id=" + _id + ", creationdate=" + creationdate + ", completiondate=" + completiondate + ", type=" + type + ", upvotes=" + upvotes + '}';
    }
    
    
}
