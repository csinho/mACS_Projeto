(function() {
    'use strict';
    
    angular.module("app").controller("DeviceController", DeviceController);

    DeviceController.$inject = ['$uibModal', '$scope', '$http', 'Paginacao', '$filter', '$window' ];
    
    function DeviceController($uibModal, $scope, $http, Paginacao, $window, $filter) {
    	var me = this;
    	
    	me.adicionar = adicionar;
    	me.estaEditandoAdicionando = estaEditandoAdicionando;
    	me.iniciar = iniciar;
    	me.cancelar = cancelar;
    	me.salvar = salvar;
    	me.editar = editar;
    	me.carregarTela = carregarTela;
    	me.carregarCombos = carregarCombos;
    	me.abrirModalConfirmarExcluirDispositivo = abrirModalConfirmarExcluirDispositivo;
    	me.cancelarExclusao = cancelarExclusao;
    	me.excluir = excluir;
    	
    	
      me.adicionando = 'adicionando';
      me.editando = 'editando';
    	me.statusTela = null;
    	me.adicionando = false;
    	me.campoBusca = null;

        // Start de funções
      me.iniciar();
      me.carregarCombos();
      me.device = {}
        

      function iniciar() {
        me.device = {};
        me.statusTela = null;

        me.paginador = Paginacao.getPaginador(function(pagina) {
          me.carregarTela(pagina);
        });

        me.carregarTela(1);
      }

      function carregarTela(pagina) {
        me.paginador.paginaAtual = pagina;

        $http.get("/devices/list", {
          params : {
            id : me.idAt,
            pagina : me.paginador.paginaAtual,
            porPagina : me.paginador.porPagina,
            imei : me.campoBusca ? me.campoBusca : ''
          }
        }).success(function(resultado) {
          me.devices = resultado.data;
          me.paginador.calcularNumeroPaginas(resultado.total);
        }).error(function(resultado) {
          toastr.error("Problema ao carregar dispositivos")
        });

      }

      function carregarCombos() {
      }
        
      function abrirModalConfirmarExcluirDispositivo(device) {
          //me.guardaIdExclusao(dispositivo);
      	me.device = device;
          me.modalConfirmarExcluirDispositivo = $uibModal.open({
            templateUrl: "modalConfirmarExcluirDispositivo.html",
            scope: $scope
          });
      }
        
        
    	function adicionar(){
    		me.statusTela = me.adicionando;
    	}
    	
    	function editar(device){
    	    me.device = angular.copy(device);
    	    me.device.ativo = me.device.status == 'active';
    	    me.statusTela = me.editando;
    	}
    	
      function cancelar() {
          me.iniciar();
      }
          
  		function estaEditandoAdicionando() {
  			return me.statusTela == me.editando || me.statusTela == me.adicionando;
  		}
		
  		function salvar(){
  		  if(me.statusTela == me.adicionando) {
  		    me.device.ativo = true;
  		  }
			
  			// Isso ainda precisa ser filtrado!
  			if ($('.ng-invalid-required').length > 0) {
  				toastr.warning("Campos obrigatórios sem preenchimento!");
  				return;
  			}
  			
  			me.device.status = (me.device.ativo)? 'active': 'inactive';

  			$http.post("/devices/", JSON.stringify(me.device))
  				.success(function(data) {
  						toastr.success("O dispositivo foi salvo com sucesso");
  						me.iniciar();
  				})
  				.error(function(data) {
  				  toastr.error(data.msg);
  				}
  			);			
  		}
		
			
	    function cancelarExclusao() {
	        me.usuario = null;
	        me.modalConfirmarExcluirDispositivo.close();
	    }
	    
	    function excluir() {
	        $http.delete("/devices/" + me.device.id)
	        .success(function(data) {
	            me.iniciar();
	            toastr.success("Dispositivo excluído com sucesso");
	            me.modalConfirmarExcluirDispositivo.close();
	        })
	        .error(function() {
	          toastr.error("Houve problema ao excluir o dispositivo");
	          me.modalConfirmarExcluirDispositivo.close();
	        })
	    }
	    
	    me.dismiss = function(){
	    	me.modalConfirmarExcluirDispositivo.close();
	    }
    }
    
})();