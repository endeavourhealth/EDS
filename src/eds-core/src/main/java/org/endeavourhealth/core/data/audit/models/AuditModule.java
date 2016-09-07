package org.endeavourhealth.core.data.audit.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum AuditModule implements IAuditModule{
	EdsUi,
	EdsPatientUi;

	public static List<IAuditModule> allSubModules() {
		List<IAuditModule> subModules = new ArrayList<>();
		Collections.addAll(subModules, EdsUiModule.values());
		Collections.addAll(subModules, EdsPatientUiModule.values());
		return subModules;
	}

	public IAuditModule getParent() { return null; }

	public enum EdsUiModule implements IAuditModule {
		Security,
		Dashboard,
		Admin,
		Organisation,
		Folders,
		EntityMap,
		Library,
		Monitoring,
		Rabbit,
		Stats,
		PatientIdentity,
		User,
		Service,
		Resource,
		Config,
		Audit;

		public IAuditModule getParent() { return AuditModule.EdsUi; }
	}

	public enum EdsPatientUiModule implements IAuditModule {
		Security,
		MedicalRecord;

		public IAuditModule getParent() { return AuditModule.EdsPatientUi; }
	}
}
