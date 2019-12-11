(function() {

	angular.module("app").service("UsuarioService", [ '$http', '$log', function($http) {

		this.usuarioLogado = function() {
			return $http.get('/usuarios/usuariologadoatualizado');
		};
		
		this.atualizarApiToken = function(usuario){
			return $http.post('/usuario/atualizarapitoken/', JSON.stringify(usuario));
		};
		
		this.atualizarApiTokens = function() {
			return $http.get('/usuario/atualizarapitoken/');
		};
		
		this.removerApiToken = function(usuario){
			return $http.post("/usuarios/removerApiToken/", JSON.stringify(usuario));
		};
		
	} ]);
})();