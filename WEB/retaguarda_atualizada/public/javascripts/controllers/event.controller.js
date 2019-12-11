(function() {
  
    'use strict';
    
    angular.module("app").controller("EventController", EventController);

    EventController.$inject = ['$scope', '$http', 'Event', 'Paginacao', '$uibModal'];

    function EventController($scope, $http, Event, Paginacao, $uibModal) {
      var me = this;
      
      me.loadedImg = '';
      
      // Constants
      me.adicionando = 'adicionando';
      me.editando = 'editando';
      
      // Atributes
      me.statusTela = null;
      me.events = [];
      me.event = {};
            
      // Published Functions
      me.iniciar = iniciar;
      me.carregarTela = carregarTela;
      me.estaEditandoAdicionando = estaEditandoAdicionando;
      me.adicionar = adicionar;
      me.editar = editar;
      me.salvar = salvar;
      me.cancelar = cancelar;
      
      // Published functions for target audience selection
      me.selectingUnidadeSaude = selectingUnidadeSaude;
      me.changingUnidadeSaude = changingUnidadeSaude;
      me.showingMunicipio = showingMunicipio;
      me.showingUnidadeSaude = showingUnidadeSaude;
      me.showingEquipe = showingEquipe;
      
      me.abrirModalConfirmarExcluirEvent = abrirModalConfirmarExcluirEvent;
      me.cancelarExclusao = cancelarExclusao;
      me.excluir = excluir;
      
      me.iniciar();

      function iniciar() {
        me.event = {};
        me.statusTela = null;

        me.paginador = Paginacao.getPaginador(function(pagina) {
          me.carregarTela(pagina);
        });
        me.carregarTela(1);
        
        angular.element(document.querySelector('#fileInput')).on('change', handleFileSelect);
      }
      
      function carregarTela(pagina) {
        me.paginador.paginaAtual = pagina;

        var params = {
          pagina : me.paginador.paginaAtual,
          porPagina : me.paginador.porPagina,
        }
        
        Event.list(params.pagina).success(function(response){
          me.events = response.lista;
          me.paginador.calcularNumeroPaginas(response.total);
        }).error(function(){
          toastr.error("Problema ao carregar eventos");
        });
      }
      

      /**
       * It is useful to know if the user is editing or adding so it can show
       * the form / hide the listing
       */
      function estaEditandoAdicionando() {
        return me.statusTela === me.editando || me.statusTela === me.adicionando;
      }

      /**
       * We just set the status so the view can show the form
       */
      function adicionar(){
        me.statusTela = me.adicionando;
      }

      /**
       * This is triggered when the user clicks on an event.
       * We make a copy of the event and change the state.
       */
      function editar(event){
        me.event = angular.copy(event);
        me.event.date = new Date(me.event.date);
        me.statusTela = me.editando;

        if(me.event.imageName){
          me.loadedImg = '/events/image?img=' + me.event.imageName;
        }
        
        angular.element(document.querySelector('#fileInput')).val(null);
      }
      
      
      function salvar(){
        if ($('.ng-invalid-required').length > 0) {
          toastr.warning("Campos obrigatórios sem preenchimento!");
          return;
        }
        
        Event.save(me.event).success(function(data){
          if(data && !data.success){
            toastr.warning(data.msg);
          }
          else {
            toastr.success("Evento cadastrado com sucesso");
            me.iniciar();
          }
        }).error(function(error){
          toastr.error(error.msg);
        });
      }
      
      function cancelar() {
        me.loadedImg = '';
        me.iniciar();
      }
      
      
      function handleFileSelect(evt) {
        var file = evt.currentTarget.files[0];
        var reader = new FileReader();
        reader.onload = function (evt) {
          $scope.$apply(function(){
            me.loadedImg = evt.target.result;
          });
        };
        reader.readAsDataURL(file);
      };
      
      /**
       * Carrega as equipes baseada na unidade de saúde passada
       */
      function carregarEquipes(unidadeSaude){
        var id = unidadeSaude.id;
        $http.get('/equipes/list/'+id).success(function(result){
          me.equipes = result;
        }).error(function(){
          toastr.error("Problema ao carregar as equipes");
        });
      }
      
      /**
       * Quando uma unidade de saude é selecionada, devemos carregar as equipes
       * correspondentes
       */
      function selectingUnidadeSaude($item) {
        if($item) {
          carregarEquipes($item);
        }
      }
      
      /**
       * Quando ocorre uma mudança na unidade de saude selecionada, deve-se 
       * apagar a equipe selecionada para evitar inconsistências.
       */
      function changingUnidadeSaude(){
        me.event.equipe = null;
      }
      

      /**
       * Retorna se o campo "Município" da seleção de público alvo deve ser exibido
       */
      function showingMunicipio(){
        return true;
      }

      /**
       * Retorna se o campo "Unidade de Saúde" da seleção de público alvo deve ser exibido
       */
      function showingUnidadeSaude(){
        if(me.event && me.event.municipio) return true;
        return false;
      }
      
      /**
       * Retorna se o campo "Equipe" da seleção de público alvo deve ser exibido
       */
      function showingEquipe(){
        if(me.event && me.event.unidadeSaude) return true;
        return false;
      }
          
      /**
       * Abre um modal para perguntar se o usuário deseja mesmo excluir evento
       */
      function abrirModalConfirmarExcluirEvent(event){
        me.event = angular.copy(event);
        me.event.date = new Date(me.event.date);
        me.modalConfirmarExcluirEvent = $uibModal.open({
          templateUrl: "modalConfirmarExcluirEvent.html",
          scope: $scope
        });
      }
      
      /**
       * Ao cancelar, resetamos o evento selecionado e fechamos o modal
       */
      function cancelarExclusao() {
        me.event = null;
        me.modalConfirmarExcluirEvent.close();
      }
      
      /**
       * Envia uma requisição para excluir o evento
       */
      function excluir() {
        Event.remove(me.event.id).success(function(data) {
            if (data.success) {
              me.iniciar();
              toastr.success(data.msg);
            } else if (data.erro) {
              toastr.error(data.msg);
            } else {
              toastr.warning(data.msg);
            }
            me.modalConfirmarExcluirEvent.close();
          })
          .error(function() {
            toastr.error("Erro ao excluir o evento!");
            me.modalConfirmarExcluirEvent.close();
          });
      }
      
      
      $scope.format = 'dd/MM/yyyy';

      $scope.dateOptions = {
        formatYear: 'yy',
        maxDate: new Date(2100, 12, 31),
        minDate: new Date(2016, 9, 1),
        startingDay: 1
      }; 
      
      $scope.datePopupOpened = false;

      $scope.openDatePopup = function() {
        $scope.datePopupOpened = true;      
      };

    }   
})();