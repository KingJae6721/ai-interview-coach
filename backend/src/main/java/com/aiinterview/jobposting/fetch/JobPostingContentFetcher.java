package com.aiinterview.jobposting.fetch;

public interface JobPostingContentFetcher {

    FetchedJobPostingContent fetch(String postingUrl);
}
