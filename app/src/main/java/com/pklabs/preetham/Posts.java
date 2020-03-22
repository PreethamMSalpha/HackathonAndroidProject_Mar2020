package com.pklabs.preetham;

public class Posts {
    public String uid,time,date
            ,postImage, description, dueDate, profileImage,
            fullName;

    public Posts(){

    }

    public Posts(String uid, String time, String date, String postImage, String description,String dueDate, String profileImage, String fullName) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postImage = postImage;
        this.description = description;
        this.dueDate = dueDate;
        this.profileImage = profileImage;
        this.fullName = fullName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getUId() {
        return uid;
    }

    public void setUId(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
