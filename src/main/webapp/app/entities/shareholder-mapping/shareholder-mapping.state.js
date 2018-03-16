(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('shareholder-mapping', {
            parent: 'entity',
            url: '/shareholder-mapping',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'adyenMiraklConnectorApp.shareholderMapping.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/shareholder-mapping/shareholder-mappings.html',
                    controller: 'ShareholderMappingController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('shareholderMapping');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('shareholder-mapping-detail', {
            parent: 'shareholder-mapping',
            url: '/shareholder-mapping/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'adyenMiraklConnectorApp.shareholderMapping.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/shareholder-mapping/shareholder-mapping-detail.html',
                    controller: 'ShareholderMappingDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('shareholderMapping');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ShareholderMapping', function($stateParams, ShareholderMapping) {
                    return ShareholderMapping.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'shareholder-mapping',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('shareholder-mapping-detail.edit', {
            parent: 'shareholder-mapping-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/shareholder-mapping/shareholder-mapping-dialog.html',
                    controller: 'ShareholderMappingDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ShareholderMapping', function(ShareholderMapping) {
                            return ShareholderMapping.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('shareholder-mapping.new', {
            parent: 'shareholder-mapping',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/shareholder-mapping/shareholder-mapping-dialog.html',
                    controller: 'ShareholderMappingDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                miraklShopId: null,
                                miraklUboNumber: null,
                                adyenShareholderCode: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('shareholder-mapping', null, { reload: 'shareholder-mapping' });
                }, function() {
                    $state.go('shareholder-mapping');
                });
            }]
        })
        .state('shareholder-mapping.edit', {
            parent: 'shareholder-mapping',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/shareholder-mapping/shareholder-mapping-dialog.html',
                    controller: 'ShareholderMappingDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ShareholderMapping', function(ShareholderMapping) {
                            return ShareholderMapping.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('shareholder-mapping', null, { reload: 'shareholder-mapping' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('shareholder-mapping.delete', {
            parent: 'shareholder-mapping',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/shareholder-mapping/shareholder-mapping-delete-dialog.html',
                    controller: 'ShareholderMappingDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ShareholderMapping', function(ShareholderMapping) {
                            return ShareholderMapping.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('shareholder-mapping', null, { reload: 'shareholder-mapping' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
