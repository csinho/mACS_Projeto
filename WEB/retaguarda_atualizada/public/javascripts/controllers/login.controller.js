(function() {
    'use strict';
    angular.module("app").controller("LoginController", LoginController);

    function LoginController($scope, $http, Spinner, $window, $filter, $timeout, $uibModal) {
        var me = this;
        me.entrar = entrar;
        me.enviarRecuperacao = enviarRecuperacao;
        me.iniciar = iniciar;
        me.abrirModalRecuperarSenha = abrirModalRecuperarSenha;
        me.fecharModalRecuperarSenha = fecharModalRecuperarSenha;
        me.iniciar();

        function iniciar() {
            me.login = {
                login: "",
                pass: ""
            };
            me.usuarioRecuperar = null;
            me.submiting = false;
        }

        function entrar() {
            if ($('.ng-invalid-required').length > 0) {
                toastr.warning("Campos obrigatórios sem preenchimento!");
                return;
            }
            me.submiting = true;
            Spinner.show('Processando o login, favor esperar.');
            $http.post('/login', me.login).success(function(data) {
                if (data.primeiroAcesso) {
                    Spinner.hide();
                    Spinner.show("Primeiro acesso, redirecionando para alterar senha.")
                    $timeout(function() {
                        window.location = "/logins/recuperar/" + data.token;
                    }, 3000);
                    return;
                }
                if (data.success) window.location.replace("/");
                else if (data.erro) {
                    me.submiting = false;
                    toastr.error(data.msg);
                    Spinner.hide();
                } else {
                    me.submiting = false;
                    toastr.warning(data.msg);
                    Spinner.hide();
                }
            }).error(function(data) {
                Spinner.hide();
                me.submiting = false;
                toastr.error('Erro ao tentar efetuar o login, favor tentar novamente daqui a alguns minutos. Caso o erro persista favor encontrar em contato com o suporte.');
            });
        }

        function enviarRecuperacao(event) {
            event.preventDefault();
            if (!me.usuarioRecuperar) {
                toastr.error("É necessário inserir um usuário válido.");
                return;
            }
            $http.get("/logins/enviarrecuperacaosenha/" + me.usuarioRecuperar).success(function(resultado) {
                if (resultado.success) {
                    toastr.success(resultado.msg);
                    me.iniciar();
                    angular.element("#esqueciSenha").modal('hide');
                } else if (resultado.erro) {
                    toastr.error(resultado.msg);
                    Spinner.hide();
                } else {
                    toastr.warning(resultado.msg);
                    Spinner.hide();
                }
            }).error(function() {
                toastr.error("Erro na solicitação.")
            })
        }

        function abrirModalRecuperarSenha() {
          me.modalRecuperarSenha = $uibModal.open({
            templateUrl: "modalRecuperarSenha.html",
            scope: $scope
          })
        }

        function fecharModalRecuperarSenha() {
          me.modalRecuperarSenha.close();
        }
    }
})();