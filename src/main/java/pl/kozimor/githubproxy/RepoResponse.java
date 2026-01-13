package pl.kozimor.githubproxy;

import java.util.List;

record RepoResponse(
    String repositoryName,
    String ownerLogin,
    List<BranchResponse> branches
) {
    
}
