package de.a12.studio.dataservices;

import de.a12.studio.dataservices.projects.ProjectsService;

public class services {

  private ProjectsService projectsService = new ProjectsService();

  public ProjectsService getProjectsService() {
    return projectsService;
  }
}
