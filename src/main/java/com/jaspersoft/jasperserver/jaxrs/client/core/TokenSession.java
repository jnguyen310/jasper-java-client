package com.jaspersoft.jasperserver.jaxrs.client.core;

import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.AbstractAdapter;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.attributes.AttributesService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.authority.organizations.OrganizationsService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.authority.roles.RolesService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.authority.users.UsersService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.bundles.BundlesService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.connections.ConnectionsService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.diagnostic.DiagnosticService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.domain.DomainMetadataService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.importexport.exportservice.ExportService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.importexport.importservice.ImportService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.inputControls.InputControlsService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.jobs.JobsService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.permissions.PermissionsService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.query.QueryExecutorService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.reporting.ReportingService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.resources.ResourcesService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.serverInfo.ServerInfoService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.settings.SettingsService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.thumbnails.ThumbnailsService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

public class TokenSession extends AnonymousSession {

    public TokenSession(SessionStorage storage) {
        super(storage);
    }

    public Client rawClient() {
        return storage.getRawClient();
    }

    public WebTarget configuredClient() {
        return storage.getConfiguredClient();
    }

    public OrganizationsService organizationsService() {
        return getService(OrganizationsService.class);
    }

    public UsersService usersService() {
        return getService(UsersService.class);
    }

    public RolesService rolesService() {
        return getService(RolesService.class);
    }

    public PermissionsService permissionsService() {
        return getService(PermissionsService.class);
    }

    public ExportService exportService() {
        return getService(ExportService.class);
    }

    public ImportService importService() {
        return getService(ImportService.class);
    }

    public ReportingService reportingService() {
        return getService(ReportingService.class);
    }

    public ResourcesService resourcesService() {
        return getService(ResourcesService.class);
    }

    public JobsService jobsService() {
        return getService(JobsService.class);
    }

    public DomainMetadataService domainService() {
        return getService(DomainMetadataService.class);
    }

    public QueryExecutorService queryExecutorService() {
        return getService(QueryExecutorService.class);
    }

    public ThumbnailsService thumbnailsService() {
        return getService(ThumbnailsService.class);
    }

    public AttributesService attributesService() {
        return getService(AttributesService.class);
    }

    public InputControlsService inputControlsService() {return getService(InputControlsService.class);}

    public DiagnosticService diagnosticService() {return getService(DiagnosticService.class);}

    public ConnectionsService connectionsService() {return getService(ConnectionsService.class);}
}
