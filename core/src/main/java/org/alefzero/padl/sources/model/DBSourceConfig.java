package org.alefzero.padl.sources.model;

import java.util.Arrays;
import java.util.List;

import org.alefzero.padl.config.model.PadlSourceConfig;

public class DBSourceConfig extends PadlSourceConfig {

	private String dnFormat;
	private String query;
	private String idColumn;
	private String[] attributes;
	private List<JoinData> joinData;
	private List<String> objectClasses;

	public String getDnFormat() {
		return dnFormat;
	}

	public void setDnFormat(String dnFormat) {
		this.dnFormat = dnFormat;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getIdColumn() {
		return idColumn;
	}

	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}

	public String[] getAttributes() {
		return attributes;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	public List<JoinData> getJoinData() {
		return joinData;
	}

	public void setJoinData(List<JoinData> joinData) {
		this.joinData = joinData;
	}

	public List<String> getObjectClasses() {
		return objectClasses;
	}

	public void setObjectClasses(List<String> objectClasses) {
		this.objectClasses = objectClasses;
	}

	@Override
	public String toString() {
		return "DBSourceConfig [dnFormat=" + dnFormat + ", query=" + query + ", idColumn=" + idColumn + ", attributes="
				+ Arrays.toString(attributes) + ", joinData=" + joinData + ", objectClasses=" + objectClasses + "]";
	}

	public static class JoinData {

		private String id;
		private String query;
		private String idJoinColumnFilter;
		private String[] attributes;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public String getIdJoinColumnFilter() {
			return idJoinColumnFilter;
		}

		public void setIdJoinColumnFilter(String idJoinColumnFilter) {
			this.idJoinColumnFilter = idJoinColumnFilter;
		}

		public String[] getAttributes() {
			return attributes;
		}

		public void setAttributes(String[] attributes) {
			this.attributes = attributes;
		}

		@Override
		public String toString() {
			return "JoinData [id=" + id + ", query=" + query + ", idJoinColumnFilter=" + idJoinColumnFilter
					+ ", attributes=" + Arrays.toString(attributes) + "]";
		}

	}

}
