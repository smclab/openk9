package io.openk9.datasource.model;

import io.openk9.datasource.searcher.util.JWT;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Map;

public enum UserField {
	NAME {
		@Override
		public void apply(
			DocTypeField docTypeField, JWT jwt,
			BoolQueryBuilder boolQueryBuilder) {

			String givenName = jwt.getGivenName();

			UserField.apply(docTypeField, givenName, boolQueryBuilder);
		}
	},
	SURNAME {
		@Override
		public void apply(
			DocTypeField docTypeField, JWT jwt,
			BoolQueryBuilder boolQueryBuilder) {

			String familyName = jwt.getFamilyName();

			UserField.apply(docTypeField, familyName, boolQueryBuilder);

		}
	},
	NAME_SURNAME {
		@Override
		public void apply(
			DocTypeField docTypeField, JWT jwt,
			BoolQueryBuilder boolQueryBuilder) {

			String name_surname =
				jwt.getGivenName() + " " + jwt.getFamilyName();

			UserField.apply(docTypeField, name_surname, boolQueryBuilder);

		}
	},
	USERNAME {
		@Override
		public void apply(
			DocTypeField docTypeField, JWT jwt,
			BoolQueryBuilder boolQueryBuilder) {

			String username = jwt.getPreferredUsername();

			UserField.apply(docTypeField, username, boolQueryBuilder);

		}
	},
	EMAIL {
		@Override
		public void apply(
			DocTypeField docTypeField, JWT jwt,
			BoolQueryBuilder boolQueryBuilder) {

			String email = jwt.getEmail();

			UserField.apply(docTypeField, email, boolQueryBuilder);
		}
	},
	ROLES {
		@Override
		public void apply(
			DocTypeField docTypeField, JWT jwt,
			BoolQueryBuilder boolQueryBuilder) {

			Map<String, List<String>> realmAccess = jwt.getRealmAccess();

			if (realmAccess != null) {
				List<String> roles = realmAccess.get("roles");
				UserField.apply(docTypeField, roles, boolQueryBuilder);
			}

		}
	};

	public abstract void apply(
		DocTypeField docTypeField, JWT jwt, BoolQueryBuilder boolQueryBuilder);

	public static void apply(
		DocTypeField docTypeField,
		String value,
		BoolQueryBuilder boolQueryBuilder) {

		if (value != null && !value.isBlank()) {
			boolQueryBuilder.should(
				QueryBuilders.termQuery(docTypeField.getFieldName(), value));
		}

	}

	public static void apply(
		DocTypeField docTypeField,
		List<String> values,
		BoolQueryBuilder boolQueryBuilder) {

		if (values != null && !values.isEmpty()) {
			boolQueryBuilder.should(
				QueryBuilders.termsQuery(docTypeField.getFieldName(), values));
		}

	}

}
