package pl.kozimor.githubproxy;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
class GithubClient {
 
        private final RestClient restClient;

        GithubClient(RestClient restClient) {
            this.restClient = restClient;
        }

        GithubRepo[] getUserRepos(String username) {
            try {
                return restClient.get()
                        .uri("/users/{username}/repos", username)
                        .retrieve()
                        .body(GithubRepo[].class);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 404) {
                    throw new UserNotFoundException("User " + username + " not found");
                }
                throw e;
            }

        }

        GithubBranch[] getUserBranches (String username, String reponame){
                return restClient.get()
                .uri("/repos/{owner}/{repo}/branches", username, reponame)
                .retrieve()
                .body(GithubBranch[].class);
            }
}

