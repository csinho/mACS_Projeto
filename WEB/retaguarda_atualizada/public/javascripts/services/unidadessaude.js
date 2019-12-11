/**
 * Camada de Abstração para comunicação com o servidor 
 **/
(function() {

	/**
	 * Serviço da tela de unidade de saúde
	 */
	angular.module("app").service("US", [ '$http', '$log', function($http) {

		/**
		 * Pesquisa unidades de saúde
		 ***/
		this.pesquisar = function(filtro) {
			filtro.term = filtro.nome;
			return $http.get('/unidadesaude/searchByUnidade',{params:filtro});
		};
		
		/**
		 * Listar todas as unidades saude
		 ***/
		this.salvar = function(unidade) {
			return $http.post('/unidadesaude/salvar',JSON.stringify(unidade));
		};
		/**
		 * Get unidade de Saúde
		 * Retorna a Unidade de Saúde
		 ***/
		this.get = function(idUnidade) {
			return $http.get('/unidadesaude/get/'+idUnidade);
		};
		
		
	} ]);
})();