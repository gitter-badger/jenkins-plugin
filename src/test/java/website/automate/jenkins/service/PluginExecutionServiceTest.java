package website.automate.jenkins.service;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import website.automate.jenkins.logging.NoOpLogHandler;
import website.automate.jenkins.service.PluginExecutionService;
import website.automate.manager.api.client.JobManagementRemoteService;
import website.automate.manager.api.client.model.Authentication;
import website.automate.manager.api.client.model.Job;
import website.automate.manager.api.client.model.Job.JobProfile;
import website.automate.manager.api.client.model.Job.JobStatus;
import website.automate.manager.api.client.model.Job.TakeScreenshots;
import website.automate.manager.api.client.model.TestResults;

@RunWith(MockitoJUnitRunner.class)
public class PluginExecutionServiceTest {

    private static final String
        JOB_TITLE = "Awesome job",
        JOB_ID = "cryptic job id",
        SCENARIO_ID = "cryptic scenario id",
        USERNAME = "random@joe.com",
        PASSWORD = "secr3t"; 
    
    @InjectMocks
    private PluginExecutionService executionService;
    
    @Mock
    private JobManagementRemoteService jobManagementRemoteService; 
    
    @Test
    public void executionShouldSucceed(){
        Authentication principal = Authentication.of(USERNAME, PASSWORD);
        List<Job> jobs = asList(createJob(SCENARIO_ID));
        when(jobManagementRemoteService.createJobs(jobs, principal))
            .thenReturn(asList(createJob(JOB_ID, JOB_TITLE, SCENARIO_ID, JobStatus.SCHEDULED)));
        when(jobManagementRemoteService.getJobsByIdsAndPrincipal(asList(JOB_ID), principal, JobProfile.BRIEF))
            .thenReturn(asList(createJob(JOB_ID, JOB_TITLE, SCENARIO_ID, JobStatus.SUCCESS)));
        when(jobManagementRemoteService.getJobsByIdsAndPrincipal(asList(JOB_ID), principal, JobProfile.COMPLETE))
            .thenReturn(asList(createJob(JOB_ID, JOB_TITLE, SCENARIO_ID, JobStatus.SUCCESS)));
        
        executionService.execute(
        		asList(SCENARIO_ID), 
        		principal,
        		NoOpLogHandler.getInstance());
        
        
    }
    
    private Job createJob(String scenarioId){
        Job job = new Job();
        job.setScenarioId(scenarioId);
        job.setTakeScreenshots(TakeScreenshots.ON_FAILURE);
        return job;
    }
    
    private Job createJob(String id, String title, String scenarioId, JobStatus status){
        Job job = new Job();
        job.setId(id);
        job.setTitle(title);
        job.setScenarioId(scenarioId);
        job.setStatus(status);
        
        TestResults results = new TestResults();
        results.setFailed(JobStatus.SUCCESS == status ? false : true);
        job.setTestResults(results);
        return job;
    }
}
