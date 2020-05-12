alter table observation add index ix_observation_parent_observation_id (parent_observation_id)

-- the below "standard" indexes aren't required by the Know Diabetes SQL, so have been dropped for performance reasons
alter table observation
drop index observation_core_concept_id;

alter table observation
drop index observation_core_concept_id_is_problem;

alter table observation
drop index observation_core_concept_id_result_value;

alter table observation
drop index ix_observation_clinical_effective_date;

alter table observation
drop index ix_observation_person_id;