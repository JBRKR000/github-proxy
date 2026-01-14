package pl.kozimor.githubproxy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "github.base-url=http://localhost:8081"
)
class GithubProxyApplicationIT {


    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8081))
            .build();


    @Autowired
    private WebApplicationContext ctx;
	private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        wireMock.resetAll();
    }

    @Test
    void shouldReturnOnlyNonForkRepositoriesWithBranches() throws Exception {
        String username = "testuser";
        wireMock.stubFor(get(urlPathEqualTo("/users/testuser/repos"))
                .willReturn(okJson("""
                    [
                      { "name": "forked-repo", "fork": true,  "owner": { "login": "testuser" } },
                       { "name": "main-repo",   "fork": false, "owner": { "login": "testuser" } }
                    ]
                """)));

  
        wireMock.stubFor(get(urlPathEqualTo("/repos/testuser/main-repo/branches"))
                .willReturn(okJson("""
                    [
                       { "name": "main", "commit": { "sha": "abc123" } }
                    ]
                """)));

        mockMvc.perform(get("/users/{username}/repositories", username))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))

                .andExpect(jsonPath("$.length()").value(1))
                 .andExpect(jsonPath("$[0].repositoryName").value("main-repo"))
                .andExpect(jsonPath("$[0].ownerLogin").value("testuser"))

                .andExpect(jsonPath("$[0].branches.length()").value(1))
                .andExpect(jsonPath("$[0].branches[0].name").value("main"))
                .andExpect(jsonPath("$[0].branches[0].lastCommitSha").value("abc123"));

        wireMock.verify(0, getRequestedFor(urlPathEqualTo("/repos/testuser/forked-repo/branches")));
    }

    @Test
    void shouldReturn404WithRequiredBodyWhenGithubUserDoesNotExist() throws Exception {
        String username = "nonexistentuser";
        wireMock.stubFor(get(urlPathEqualTo("/users/nonexistentuser/repos"))
                .willReturn(notFound()));
        mockMvc.perform(get("/users/{username}/repositories", username))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldProcessRequestsInParallel() throws Exception {
        String username = "testuser";
        
        wireMock.stubFor(get(urlPathEqualTo("/users/testuser/repos"))
                .willReturn(okJson("""
                    [
                      { "name": "forked-repo", "fork": true,  "owner": { "login": "testuser" } },
                      { "name": "repo-one",    "fork": false, "owner": { "login": "testuser" } },
                      { "name": "repo-two",    "fork": false, "owner": { "login": "testuser" } }
                    ]
                """)
                .withFixedDelay(1000)));

        wireMock.stubFor(get(urlPathEqualTo("/repos/testuser/repo-one/branches"))
                .willReturn(okJson("""
                    [
                       { "name": "main", "commit": { "sha": "abc123" } },
                       { "name": "develop", "commit": { "sha": "def456" } },
                       { "name": "feature", "commit": { "sha": "ghi789" } }
                    ]
                """)
                .withFixedDelay(1000)));

        wireMock.stubFor(get(urlPathEqualTo("/repos/testuser/repo-two/branches"))
                .willReturn(okJson("""
                    [
                       { "name": "main", "commit": { "sha": "xyz999" } }
                    ]
                """)
                .withFixedDelay(1000)));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        mockMvc.perform(get("/users/{username}/repositories", username))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].repositoryName").value("repo-one"))
                .andExpect(jsonPath("$[0].branches.length()").value(3))
                .andExpect(jsonPath("$[1].repositoryName").value("repo-two"))
                .andExpect(jsonPath("$[1].branches.length()").value(1));

        stopWatch.stop();
        long totalTime = stopWatch.getTime();
        wireMock.verify(3, getRequestedFor(urlMatching(".*")));
        assertTrue(totalTime >= 2000,
                "Expected time >= 2000ms, but was " + totalTime + "ms");
        assertTrue(totalTime < 3000,
                "Expected time < 3000ms, but was " + totalTime + "ms");
    }
}
