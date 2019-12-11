/**
 * Camada de Abstração para comunicação com o servidor 
 **/
(function() {

	angular.module("app").service("Event", [ '$http', '$log', function($http) {

		/**
		 * List events 
		 **/
		this.list = function(page) {
			return $http.get('/events/list/' + page);
		};
		
		/**
		 * Save event 
		 **/
		this.save = function(event) {
			return $http.post('/events/save', event);
		};
		
		/**
		 * Remove event 
		 **/
		this.remove = function(idEvent) {
			return $http.post('/events/delete/' + idEvent);
		};
	} ]);
})();