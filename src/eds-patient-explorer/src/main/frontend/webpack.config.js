var webpack = require("webpack");
var HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
	entry: {
		'common' : './app/common/common.module.ts',
		'config' : './app/config/config.module.ts',
		'security' : './app/security/security.module.ts',
		'dialogs' : './app/dialogs/dialogs.module.ts',

		'folder' : './app/folder/folder.module.ts',
		'coding' : './app/coding/coding.module.ts',
		'practitioner' : './app/practitioner/practitioner.module.ts',
		'library' : './app/library/library.module.ts',

		'patientIdentity' : './app/patientIdentity/patientIdentity.module.ts',
		'countReport' : './app/countReport/countReport.module.ts',
		'recordViewer' : './app/recordViewer/recordViewer.module.ts',
		'resources' : './app/resources/resources.module.ts',
		'sqlEditor' : './app/sqlEditor/sqlEditor.module.ts',

		'layout' : './app/layout/layout.module.ts',

		'app' : './app/patientExplorer.app.ts',
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
				'app',
				'layout',
				'patientIdentity', 'countReport', 'recordViewer', 'resources', 'sqlEditor',
				'folder', 'coding', 'practitioner', 'library',
				'dialogs', 'security', 'config', 'common',
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