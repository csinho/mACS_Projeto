(function() {
    'use strict';

    angular.module("app").controller("UsuariosController", UsuariosController);

    UsuariosController.$inject = ['$uibModal', '$scope', '$http', 'Paginacao', 'US', 'UsuarioService', '$timeout'];

    function UsuariosController($uibModal, $scope, $http, Paginacao, US, UsuarioService, $timeout) {
        var me = this;
        me.usuarioLogado = {};

        me.desabilitarBTNAtualizarToken = false;

        //Variáveis para configuração do token.
        me.listaDias = [];

        // Atributos estáticos
        me.adicionando = 'adicionando';
        me.editando = 'editando';
        me.usuario = {};
        me.perfils = [];
        me.perfilsAux = {};
        me.usuarios = [];
        me.statusTela = null;
        me.perfilsAux = [];
        me.usuarioNome = null
        me.userTypes = [];


        // Funções
        me.iniciar = iniciar;
        me.estaEditandoAdicionando = estaEditandoAdicionando;
        me.salvar = salvar;
        me.cancelar = cancelar;
        me.adicionarUsuario = adicionarUsuario;
        me.editar = editar;
        me.excluir = excluir;
        me.carregarTela = carregarTela;
        me.guardaIdExclusao = guardaIdExclusao;
        me.cancelarExclusao = cancelarExclusao;
        me.carregarCombos = carregarCombos;
        me.procurarUsuarioPorLogin = procurarUsuarioPorLogin;
        me.redefinirSenha = redefinirSenha;
        me.getPerfilDescricao = getPerfilDescricao;
        me.abrirModalConfirmarExcluirUsuario = abrirModalConfirmarExcluirUsuario;

        me.showingTipo = showingTipo;
        me.showingCNS = showingCNS;
        me.showingMunicipio = showingMunicipio;
        me.showingUnidadeSaude = showingUnidadeSaude;
        me.showingEquipe = showingEquipe;
        me.showingMicroarea = showingMicroarea;

        me.changingPerfil = changingPerfil;
        me.changingUsuarioTipo = changingUsuarioTipo;
        me.changingMunicipio = changingMunicipio;
        me.selectingUnidadeSaude = selectingUnidadeSaude;
        me.changingUnidadeSaude = changingUnidadeSaude;
        me.changingEquipe = changingEquipe;
        me.buscarUsuarioLogado = buscarUsuarioLogado;

        me.atualizarUnidade = atualizarUnidade;
        me.cancelarModalUnidade = cancelarModalUnidade;

        me.permiteApenasNumeroCNS = permiteApenasNumeroCNS;

        // Start de funções
        me.iniciar();
        me.carregarCombos();

        function iniciar() {
            me.usuario = {};
            me.statusTela = null;

            me.paginador = Paginacao.getPaginador(function(pagina) {
                me.carregarTela(pagina);
            });

            me.carregarTela(1);

            me.buscarUsuarioLogado();

            popularDias();
        }

        function carregarTela(pagina) {
            me.paginador.paginaAtual = pagina;

            $http.get("/usuarios/listar", {
                params: {
                    id: me.idAt,
                    pagina: me.paginador.paginaAtual,
                    porPagina: me.paginador.porPagina,
                    nome: me.usuarioNome ? me.usuarioNome : ''
                }
            }).success(function(resultado) {
                me.usuarios = resultado.lista;
                me.paginador.calcularNumeroPaginas(resultado.total);

            }).error(function(resultado) {
                toastr.error("Problema ao carregar usuarios")
            });
        }

        function carregarCombos() {

            $http.get('/usuarios/buscarcomboperfil').success(function(resultado) {
                me.perfils = resultado;

            }).error(function() {
                toastr.error("Problema ao carregar perfils");
            });

            $http.get('/usuarios/types').success(function(result) {
                me.userTypes = result;
            }).error(function() {
                toastr.error("Problema ao carregar tipos de usuários");
            });

        }

        function carregarEquipes(unidadeSaude) {
            var id = unidadeSaude.id;
            $http.get('/equipes/list/' + id).success(function(result) {
                me.equipes = result;
            }).error(function() {
                toastr.error("Problema ao carregar as equipes");
            });
        }

        function adicionarUsuario() {
            me.statusTela = me.adicionando;
        }

        function editar(usuarioEdit) {
            me.usuario = angular.copy(usuarioEdit);

            var municipio = me.usuario.municipio;

            me.statusTela = me.editando;
        }

        function abrirModalConfirmarExcluirUsuario(usuario) {
            me.guardaIdExclusao(usuario);
            me.modalConfirmarExcluirUsuario = $uibModal.open({
                templateUrl: "modalConfirmarExcluirUsuario.html",
                scope: $scope
            })
        }

        function guardaIdExclusao(reg) {
            me.usuario = reg;
        }

        function cancelarExclusao() {
            me.usuario = null;
            me.modalConfirmarExcluirUsuario.close();
        }

        function salvar() {
            if ($('.ng-invalid-required').length > 0) {
                toastr.warning("Campos obrigatórios sem preenchimento!");
                return;
            }

            if (!me.usuario.perfis) {
                toastr.warning("Ao menos um perfil deve ser associado ao usuário!");
                return;
            }

            if (!me.usuario.perfis === 'administrador') {
                if (!cnsIsValid(me.usuario.userData.cns)) {
                    toastr.warning("CNS é inválido.");
                    return;
                }
            }
            $http.post("/usuarios/gravar", JSON.stringify(me.usuario)).success(function(data) {
                if (data.success) {
                    toastr.success(data.msg);
                    me.iniciar();
                } else if (data.erro) {
                    toastr.error(data.msg);
                } else {
                    toastr.warning(data.msg);
                }
            }).error(function() {
                toastr.error("Erro ao salvar usuario!");
            })
        }

        function permiteApenasNumeroCNS() {
            if (isNaN(me.usuario.userData.cns)) {
                me.usuario.userData.cns = me.usuario.userData.cns.substring(0, me.usuario.userData.cns.length - 1);
                permiteApenasNumeroCNS();
            }
        }

        /**
         * Custom validation to verify if the CNS is valid
         * @param  numeroCNS
         */
        function cnsIsValid(numeroCNS) {
            var s = numeroCNS;
            if (/[1-2]\d{10}00[0-1]\d/.test(s) || /[7-9]\d{14}/.test(s)) {
                var sum = 0;
                var str = s.toString();
                for (var i = 0; i < str.length; i++) {
                    var charInt = parseInt(str.charAt(i), 10);
                    sum += charInt * (15 - i);
                }
                var rest = sum % 11;
                return rest === 0;
            }
            return false;
        }

        function excluir() {
            $http.get("/usuarios/deletar", {
                params: {
                    id: me.usuario.id
                }
            }).success(function(data) {
                if (data.success) {
                    me.iniciar();
                    toastr.success(data.msg);
                } else if (data.erro) {
                    toastr.error(data.msg);
                } else {
                    toastr.warning(data.msg);
                }
                me.modalConfirmarExcluirUsuario.close();
            }).error(function() {
                toastr.error("Houve problema ao excluir o usuario");
                me.modalConfirmarExcluirUsuario.close();
            })
        }

        function cancelar() {
            me.iniciar();
        }

        function estaEditandoAdicionando() {
            var retorno = me.statusTela == me.editando || me.statusTela == me.adicionando;
            return retorno;
        }

        function procurarUsuarioPorLogin() {
            return true;
        }

        function redefinirSenha(login) {
            $http.get("/logins/enviarrecuperacaosenha/" + login).success(function(data) {
                if (data.success) {
                    me.iniciar();
                    toastr.success(data.msg);
                } else if (data.erro) {
                    toastr.error(data.msg);
                } else {
                    toastr.warning(data.msg);
                }
            }).error(function() {
                toastr.error("Houve problema ao enviar email de recuperação de senha");
            })
        }

        function getPerfilDescricao(perfilCodigo) {
            for (var i = 0; i < me.perfils.length; i++) {
                if (me.perfils[i].codigo == perfilCodigo) {
                    return me.perfils[i].descricao;
                }
            }
        }

        function changingPerfil() {
            if (me.usuario.userData) {
                me.usuario.userData = null;
            }
        }

        function changingUsuarioTipo() {
            me.usuario.userData.municipio = null;
            changingMunicipio();
        }

        function changingMunicipio() {
            me.usuario.userData.unidadeSaude = null;
            changingUnidadeSaude();
        }

        function selectingUnidadeSaude($item) {
            if ($item) {
                carregarEquipes($item);

                if (!$item.cnpj) {
                    openModalUnidade();
                }
            }
        }

        function changingUnidadeSaude() {
            me.usuario.userData.equipe = null;
        }

        function changingEquipe() {
            me.usuario.userData.microarea = null;
        }

        function checkUserData() {
            if (!me.usuario) return false;
            if (!me.usuario.userData) return false;
            return true;
        }

        function showingTipo() {
            return me.usuario.perfis === 'usuario';
        }

        function showingCNS() {
            return checkUserData();
        }

        function showingMunicipio() {
            if (!me.usuario.perfis) return false;
            if (me.usuario.perfis === 'gestor_estado' || me.usuario.perfis === 'administrador') return false;
            //if(!checkUserData()) return false;
            //if(!me.usuario.userData.tipo) return false;
            return true;
        }

        function showingUnidadeSaude() {
            if (me.usuario.perfis === 'gestor_municipio') return false;
            if (!checkUserData()) return false;
            if (!me.usuario.userData.municipio) return false;
            return true;
        }

        function showingEquipe() {
            if (!checkUserData()) return false;
            if (!me.usuario.userData.tipo) return false;
            if (me.usuario.userData.tipo.id !== 0) return false; // hardcoded for now
            if (!me.usuario.userData.unidadeSaude) return false;
            return true;
        }

        function showingMicroarea() {
            if (!checkUserData()) return false;
            if (!me.usuario.userData.equipe) return false;
            return (me.usuario.userData.tipo.id === 0);
        }

        function openModalUnidade() {
            me.unidadeFoiAtualizada = false;
            me.modalUnidadeSaude = $uibModal.open({
                templateUrl: "modalUnidadeSaude.html",
                scope: $scope
            });

            me.modalUnidadeSaude.result.catch(cancelarModalUnidade);
        }

        function cancelarModalUnidade() {
            if (me.modalUnidadeSaude) {

                if (
                    /*me.usuario && me.usuario.userData && me.usuario.userData.unidadeSaude 
                                && !me.usuario.userData.unidadeSaude.cnpj*/
                    !me.unidadeFoiAtualizada) {
                    me.usuario.userData.unidadeSaude = null;
                }

                me.modalUnidadeSaude.close();
            }
        }

        function atualizarUnidade() {
            if (me.usuario && me.usuario.userData && me.usuario.userData.unidadeSaude) {
                US.salvar(me.usuario.userData.unidadeSaude).then(function() {
                    toastr.success("Unidade de Saúde atualizada com sucesso.");
                    me.modalUnidadeSaude.close();
                }, function() {
                    toastr.error("Ocorreu um erro.");
                });
            }
        }

        me.abrirModalConfigurarToken = function() {
            $('#modalConfigurarToken').modal('show');
        }

        me.atualizarToken = function() {

            if (me.usuarioLogado.duracaoApiTokenDia == 0) {
                toastr.warning("Por favor, selecione a quantidade de dias para expiração do token.");
                return;
            }

            UsuarioService.atualizarApiToken(me.usuarioLogado).then(
                function(response) {
                    me.usuarioLogado = response.data;
                    $('#modalConfigurarToken').modal('hide');
                    toastr.success("Token atualizado com sucesso!");
                },
                function(response) {
                    toastr.error(response.data);
                });
        }

        me.removerApiToken = function() {
            UsuarioService.removerApiToken(me.usuarioLogado).then(
                function(response) {
                    me.usuarioLogado = response.data;
                    me.desabilitarBTNAtualizarToken = true;
                    toastr.success("Token removido com sucesso.");
                },
                function(response) {
                    toastr.error("Erro na operação.");
                });
        }

        me.copiarTokenAreaTransferencia = function() {

            var tokenAreaTransferencia = document.querySelector('.token');
            tokenAreaTransferencia.select();

            try {
                document.execCommand('copy');
                toastr.success("Token copiado para área de transferência.");
            } catch (err) {
                toastr.error('Falha ao copiar o token');
            }
        }

        function popularDias() {
            for (var i = 1; i < 31; i++) {
                me.listaDias.push(i);
            }
        }


        function buscarUsuarioLogado() {
            UsuarioService.usuarioLogado().then(
                function(response) {
                    me.usuarioLogado = response.data;
                    if (me.usuarioLogado.apiToken == null) {
                        me.desabilitarBTNAtualizarToken = true;
                    }
                },
                function(response) {
                    toastr.error(response.data);
                });
        }

    }
})();