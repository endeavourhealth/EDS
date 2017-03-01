var webpack = require("webpack");
var HtmlWebpackPlugin = require('html-webpack-plugin');
var CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
	entry: {
		// Base
		'config' : './app/config/config.module.ts',
		'common' : './app/common/common.module.ts',

		// Services
		'admin' : './app/administration/admin.module.ts',
		'security' : './app/security/security.module.ts',

		// Components
		'pipes' : './app/pipes/pipes.module.ts',
		'flowchart' : './app/flowchart/flowchart.module.ts',
		'folder' : './app/folder/folder.module.ts',
		'mouseCapture' : './app/mouseCapture/mouseCapture.module.ts',

		// Dialogs
		'dialogs' : './app/dialogs/dialogs.module.ts',
		'coding' : './app/coding/coding.module.ts',
		'codeSet' : './app/codeSet/codeSet.module.ts',
		'countReport' : './app/countReport/countReport.module.ts',
		'dataSet' : './app/dataSet/dataSet.module.ts',
		'expressions' : './app/expressions/expressions.module.ts',
		'protocol' : './app/protocol/protocol.module.ts',
		'query' : './app/query/query.module.ts',
		'system' : './app/system/system.module.ts',
		'tests' : './app/tests/tests.module.ts',

		// Modules
		'audit' : './app/audit/audit.module.ts',
		'dashboard' : './app/dashboard/dashboard.module.ts',
		'exchangeAudit' : './app/exchangeAudit/exchangeAudit.module.ts',
		'library' : './app/library/library.module.ts',
		'logging' : './app/logging/logging.module.ts',
		'organisations' : './app/organisations/organisations.module.ts',
		'queueing' : './app/queueing/queueing.module.ts',
		'services' : './app/services/services.module.ts',
		'stats' : './app/stats/stats.module.ts',
		'transformErrors' : './app/transformErrors/transformErrors.module.ts',
		'users' : './app/users/user.module.ts',

		// Application
		'layout' : './app/layout/layout.module.ts',
		'app' : './app/dataServiceManager.app.ts',

		// 3rd Party
		'vendor' : './app/vendor.ts',
		'shim' : 'core-js/fn/object/assign'
	},
	output: {
		filename: './[name].bundle.js',
		path: __dirname + '/../webapp'
	},
	resolve: {
		extensions: ['.webpack.js', '.web.js', '.ts', '.js']
	},
	module: {
		loaders: [
			{ test: /\.ts$/, loader: 'awesome-typescript-loader' },
			{ test: /\.html/, loader: 'raw-loader' },
			{ test: /\.css$/, loader: "style-loader!css-loader" },
			{ test: /\.less$/, loader: "style-loader!css-loader!less-loader" },
			{	test: /\.(eot|svg|ttf|woff(2)?)(\?v=\d+\.\d+\.\d+)?/, loader: 'url-loader' },
			{ test:  /\.(jpe?g|png|gif|svg)$/i, loader: 'file-loader' }
		]
	},
	externals: {
		"jquery": "jQuery"
	},
	plugins: [
		new webpack.optimize.CommonsChunkPlugin({
			name: [
				// Application
				'app', 'layout',

				// Modules
				'users', 'transformErrors', 'stats', 'services', 'queueing', 'organisations', 'logging', 'library', 'exchangeAudit', 'dashboard', 'audit',

				// Dialogs
				'tests', 'system', 'query', 'protocol', 'expressions', 'dataSet', 'countReport', 'codeSet', 'coding', 'dialogs',

				// Components
				'mouseCapture', 'folder', 'flowchart', 'pipes',

				// Services
				'security', 'admin',

				// Base
				'common', 'config',

				// 3rd Party
				'shim', 'vendor'
			]
		}),
		new webpack.ContextReplacementPlugin(
			/angular(\\|\/)core(\\|\/)(esm(\\|\/)src|src)(\\|\/)linker/,
			__dirname
		),
		new HtmlWebpackPlugin(
		{
			template: 'index.ejs',
			inject: 'body'
		}),
		new CopyWebpackPlugin([
			{ from: './api-docs', to: './api-docs' },
			{ from: './node_modules/swagger-ui/dist', to: './api-docs/swagger-ui' }
		]),
		new webpack.ProvidePlugin({
			'moment': 'moment'
		})
	],
	devServer: {
		inline: true,
		contentBase: '..\\webapp',

		proxy: {
			'/api': { target: 'http://localhost:8000' },
			'/public': { target: 'http://localhost:8000'}
		}
	}
};