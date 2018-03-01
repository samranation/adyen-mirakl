(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('mirakl-delta', {
            parent: 'entity',
            url: '/mirakl-delta',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'adyenMiraklConnectorApp.miraklDelta.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/mirakl-delta/mirakl-deltas.html',
                    controller: 'MiraklDeltaController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('miraklDelta');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('mirakl-delta-detail', {
            parent: 'mirakl-delta',
            url: '/mirakl-delta/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'adyenMiraklConnectorApp.miraklDelta.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/mirakl-delta/mirakl-delta-detail.html',
                    controller: 'MiraklDeltaDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('miraklDelta');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'MiraklDelta', function($stateParams, MiraklDelta) {
                    return MiraklDelta.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'mirakl-delta',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('mirakl-delta-detail.edit', {
            parent: 'mirakl-delta-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mirakl-delta/mirakl-delta-dialog.html',
                    controller: 'MiraklDeltaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['MiraklDelta', function(MiraklDelta) {
                            return MiraklDelta.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('mirakl-delta.new', {
            parent: 'mirakl-delta',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mirakl-delta/mirakl-delta-dialog.html',
                    controller: 'MiraklDeltaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                shopDelta: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('mirakl-delta', null, { reload: 'mirakl-delta' });
                }, function() {
                    $state.go('mirakl-delta');
                });
            }]
        })
        .state('mirakl-delta.edit', {
            parent: 'mirakl-delta',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mirakl-delta/mirakl-delta-dialog.html',
                    controller: 'MiraklDeltaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['MiraklDelta', function(MiraklDelta) {
                            return MiraklDelta.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('mirakl-delta', null, { reload: 'mirakl-delta' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('mirakl-delta.delete', {
            parent: 'mirakl-delta',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mirakl-delta/mirakl-delta-delete-dialog.html',
                    controller: 'MiraklDeltaDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['MiraklDelta', function(MiraklDelta) {
                            return MiraklDelta.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('mirakl-delta', null, { reload: 'mirakl-delta' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
