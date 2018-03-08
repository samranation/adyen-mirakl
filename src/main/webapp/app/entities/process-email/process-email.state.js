(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('process-email', {
            parent: 'entity',
            url: '/process-email',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'adyenMiraklConnectorApp.processEmail.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/process-email/process-emails.html',
                    controller: 'ProcessEmailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('processEmail');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('process-email-detail', {
            parent: 'process-email',
            url: '/process-email/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'adyenMiraklConnectorApp.processEmail.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/process-email/process-email-detail.html',
                    controller: 'ProcessEmailDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('processEmail');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ProcessEmail', function($stateParams, ProcessEmail) {
                    return ProcessEmail.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'process-email',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('process-email-detail.edit', {
            parent: 'process-email-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/process-email/process-email-dialog.html',
                    controller: 'ProcessEmailDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ProcessEmail', function(ProcessEmail) {
                            return ProcessEmail.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('process-email.new', {
            parent: 'process-email',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/process-email/process-email-dialog.html',
                    controller: 'ProcessEmailDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                to: null,
                                subject: null,
                                content: null,
                                isMultipart: null,
                                isHtml: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('process-email', null, { reload: 'process-email' });
                }, function() {
                    $state.go('process-email');
                });
            }]
        })
        .state('process-email.edit', {
            parent: 'process-email',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/process-email/process-email-dialog.html',
                    controller: 'ProcessEmailDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ProcessEmail', function(ProcessEmail) {
                            return ProcessEmail.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('process-email', null, { reload: 'process-email' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('process-email.delete', {
            parent: 'process-email',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/process-email/process-email-delete-dialog.html',
                    controller: 'ProcessEmailDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ProcessEmail', function(ProcessEmail) {
                            return ProcessEmail.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('process-email', null, { reload: 'process-email' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
