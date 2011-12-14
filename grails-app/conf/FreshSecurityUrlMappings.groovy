class FreshSecurityUrlMappings {

	static mappings = {
	    // @todo make the base url configurable
		"/auth/$action?/$id?"(controller:'auth'){
			constraints {
				// apply constraints here
			}
		}
	}
}
