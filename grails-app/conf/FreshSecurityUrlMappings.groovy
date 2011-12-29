class FreshSecurityUrlMappings {

	static mappings = {
	    // @todo make the base url configurable
		"/auth/$action?/$id?"(controller:'freshSecurityAuth'){
			constraints {
				// apply constraints here
			}
		}
	}
}
