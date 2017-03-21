package torille.fi.lurkforreddit.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model containing all response listings and next/last page
 */
public class PostData {
    private String modhash;
    @SerializedName("children")
    private List<Post> Posts;
    @SerializedName("after")
    private String nextPage;
    @SerializedName("before")
    private String lastPage;

    public PostData() {
    }

    public String getModhash() {
        return modhash;
    }

    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    public List<Post> getPosts() {
        return Posts;
    }

    public void setPosts(List<Post> Posts) {
        this.Posts = Posts;
    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    public String getLastPage() {
        return lastPage;
    }

    public void setLastPage(String lastPage) {
        this.lastPage = lastPage;
    }
}