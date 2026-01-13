package pl.kozimor.githubproxy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record GithubRepo(
    String name,
    boolean fork,
    GithubOwner owner

){

}