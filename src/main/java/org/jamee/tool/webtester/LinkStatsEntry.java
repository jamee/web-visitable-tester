package org.jamee.tool.webtester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinkStatsEntry {
	String link;
	String title;
	long maxLoadTime;
	long minLoadTime;
	long avarageLoadTime;
	int responseCode;
	int errorCount;
	int requestedCount;
	int maxRequestCount;
	int minRequestCount;
	List<LinkStatsEntry> links = new ArrayList<LinkStatsEntry>();

	public LinkStatsEntry(String link, String title, int maxRequestCount, int minRequestCount) {
		super();
		this.link = link;
		this.title = title;
		this.maxRequestCount = maxRequestCount;
		this.minRequestCount = minRequestCount;
	}

	public LinkStatsEntry(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public String getTitle() {
		return title;
	}

	public long getMaxResponseTime() {
		return maxLoadTime;
	}

	public long getMinResponseTime() {
		return minLoadTime;
	}

	public long getAvarageResponseTime() {
		return avarageLoadTime;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public int getRequestedCount() {
		return requestedCount;
	}

	public int getMaxRequestCount() {
		return maxRequestCount;
	}

	public int getMinRequestCount() {
		return minRequestCount;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMaxLoadTime(long maxResponseTime) {
		this.maxLoadTime = maxResponseTime;
	}

	public void setMinLoadTime(long minResponseTime) {
		this.minLoadTime = minResponseTime;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public void setRequestedCount(int requestedCount) {
		this.requestedCount = requestedCount;
	}

	public void setAvarageLoadTime(long avarageResponseTime) {
		this.avarageLoadTime = avarageResponseTime;
	}

	public List<LinkStatsEntry> getLinks() {
		return Collections.unmodifiableList(links);
	}

	public void addlinkStatsEntry(LinkStatsEntry linkStatsEntry) {
		this.links.add(linkStatsEntry);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LinkStatsEntry other = (LinkStatsEntry) obj;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		return true;
	}

}