package pl.kozimor.githubproxy;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
class RepoService {

    private final GithubClient githubClient;

    RepoService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepoResponse> listNonForkReposWithBranches(String username) {
        GithubRepo[] repos = githubClient.getUserRepos(username);
        if (repos == null) {
            return List.of();
        }

        List<CompletableFuture<RepoResponse>> futures = Arrays.stream(repos)
                .filter(repo -> !repo.fork())
                .map(repo -> CompletableFuture.supplyAsync(() -> {
                    GithubBranch[] branches =
                            githubClient.getUserBranches(repo.owner().login(), repo.name());

                    List<BranchResponse> branchResponses =
                            branches == null
                                    ? List.of()
                                    : Arrays.stream(branches)
                                        .map(b -> new BranchResponse(b.name(), b.commit().sha()))
                                        .toList();

                    return new RepoResponse(
                            repo.name(),
                            repo.owner().login(),
                            branchResponses
                    );
                }))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }
}
