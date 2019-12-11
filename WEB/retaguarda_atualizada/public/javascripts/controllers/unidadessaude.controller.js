/**
 * This is a directive to abstract the register page for health units.
 */
angular.module("app").directive("unidadeSaudeEdit", function(){
  return {
    templateUrl : '/public/templates/cadastroUnidadeSaude.html', 
    scope : {
      unidade : '=', 
    }
  };
});


angular.module("app").controller("UnidadesSaudeController", 
  ['$http', 'Spinner','Paginacao','US', 
  function($http, Spinner, Paginacao, US){

    var me = this;
    //constantes da tela
    const LISTAR = 1;
    const EDITAR = 2;
    
    //variaveis da tela
    me.unidades = [];
    me.filtro = {};
    me.unidade = {};
    me.statusTela = LISTAR;
  
  
    me.paginador = Paginacao.getPaginador(function(pagina) {
      me.filtro.pagina = pagina;
      me.paginador.paginaAtual = pagina;
      me.pesquisar();
    });
  
    /**
     * Carrega a tela
     */
    me.iniciar = function() {
      me.pesquisar();
      me.paginador.paginaAtual 	= 1;
  
    }
    /**
     * evento do botao pesquisar
     */
    me.pesquisar = function() {
      US.pesquisar(me.filtro).then(function(response){
  
        me.unidades = response.data.lista
  
  
  
        me.paginador.calcularNumeroPaginas(response.data.total);
      });
    }
    /**
     * evento do botao editar
     */
    me.editar = function(unidade) {
      US.get(unidade.id).then(function(response){
        me.unidade = response.data;
      });
      me.statusTela = EDITAR;
    }
    /**
     * evento do botao voltar
     */
    me.voltar = function() {
      me.unidade = {};
      me.statusTela = LISTAR;
      me.pesquisar();
    }
    /**
     * evento do botao salvar
     */
    me.salvar = function() {
      US.salvar(me.unidade).then(function(){
        toastr.success("Unidade de Saúde atualizada com sucesso.");
        me.voltar();
      },function(){
        toastr.error("Ocorreu um erro.");
      });
    }
    
    /**
     * Metodos para verificar o staus da tela
     */
    me.isEditar = function() {
      return me.statusTela === EDITAR;
    }
    me.isListar = function() {
      return me.statusTela === LISTAR;
    }

}])

