package pl.kozimor.githubproxy;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubBranch(
    String name,
    GithubCommit commit
) {
}