(function() {

  'use strict';

  angular.module("app").controller("LoginsController", LoginsController);

  LoginsController.$inject = [ '$scope', '$http', 'Spinner', 'Paginacao', '$filter', '$window', '$timeout' ];

  function LoginsController($scope, $http, Spinner, Paginacao, $window, $filter, $timeout) {

    var me = this;
    
    // Atributos estáticos
    me.usuario = {};
    me.usuario.token = angular.element("#token").val();
    me.usuario.id = angular.element("#id").val();
    //funções
    me.alterar = alterar;
    me.salvarNovaSenha = salvarNovaSenha;
    
    function alterar(event){
      
      event.preventDefault();
      
      if($('.ng-invalid-required').length > 0){
        toastr.warning("Campos obrigatórios sem preenchimento.");
        return;
      }
      
      if(me.usuario.pass.length < 6){
        toastr.warning("A senha deve possuir no mínimo 6 caracteres.");
        return;
      }
      
      var testar_password = new RegExp("^(?=.*[a-z]|[A-Z])(?=.*[0-9])(?=.{6,})");

      if(!testar_password.test(me.usuario.pass)){
        toastr.warning("A senha deve possuir letras e números.");
        return;
      }

      if(me.usuario.pass != me.usuario.passConf){
        toastr.warning("As senhas não conferem.");
        return;
      }
      Spinner.show("Aguarde...");
      $http.post("/logins/alterarsenha",JSON.stringify(me.usuario))
      .success(function(resultado){
        if(resultado.success){
          toastr.success(resultado.msg+" Redirecionando para login...");
          
          $timeout(function() {
            window.location = "/";
          }, 3000);
          
          
        }
        else if(resultado.erro){
			toastr.error(resultado.msg);
			Spinner.hide();
		} else {
			toastr.warning(resultado.msg);
			Spinner.hide();
		}
      })
      .error(function(){
        toastr.error("Erro ao alterar senha.")
      })
      
    }
    
    
    function salvarNovaSenha(event){
        
        event.preventDefault();
        
        if($('.ng-invalid-required').length > 0){
          toastr.warning("Campos obrigatórios sem preenchimento.");
          return;
        }
        
        if(me.usuario.pass.length < 6){
          toastr.warning("A senha deve possuir no mínimo 6 caracteres.");
          return;
        }
        
        var testar_password = new RegExp("^(?=.*[a-z]|[A-Z])(?=.*[0-9])(?=.{6,})");

        if(!testar_password.test(me.usuario.pass)){
          toastr.warning("A senha deve possuir letras e números.");
          return;
        }

        if(me.usuario.pass != me.usuario.passConf){
          toastr.warning("As senhas não conferem.");
          return;
        }
        Spinner.show("Aguarde...");
        $http.post("/logins/salvarNovaSenha",JSON.stringify(me.usuario))
        .success(function(resultado){
          if(resultado.success){
            toastr.success(resultado.msg+" Redirecionando para login...");
            
            $timeout(function() {
              window.location = "/";
            }, 3000);
            
            
          }
          else if(resultado.erro){
  			toastr.error(resultado.msg);
  			Spinner.hide();
  		} else {
  			toastr.warning(resultado.msg);
  			Spinner.hide();
  		}
        })
        .error(function(){
          toastr.error("Erro ao alterar senha.")
        })
        
      }     
    
  }
})();