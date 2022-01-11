import svelte from 'rollup-plugin-svelte';
import commonjs from '@rollup/plugin-commonjs';
import resolve from '@rollup/plugin-node-resolve';
import livereload from 'rollup-plugin-livereload';
import { terser } from 'rollup-plugin-terser';
import css from 'rollup-plugin-css-only';
import preprocess from 'svelte-preprocess';
import alias from "@rollup/plugin-alias";
import * as path from "path";
import replace from '@rollup/plugin-replace';
import json from '@rollup/plugin-json';
require('dotenv').config()

const production = !process.env.ROLLUP_WATCH;

function serve() {
	let server;

	function toExit() {
		if (server) server.kill(0);
	}

	return {
		writeBundle() {
			if (server) return;
			server = require('child_process').spawn('npm', ['run', 'start', '--', '--dev'], {
				stdio: ['ignore', 'inherit', 'inherit'],
				shell: true
			});

			process.on('SIGTERM', toExit);
			process.on('exit', toExit);
		}
	};
}
const projectRootDir = path.resolve(__dirname);

export default {
	input: 'src/main.js',
	output: {
		sourcemap: true,
		format: 'iife',
		name: 'app',
		file: 'public/build/bundle.js'
	},
	plugins: [
		alias({
			resolve: ['.js','.svelte' ],
			entries: [
				{find:'src', replacement:path.resolve(projectRootDir, "src")},
				{find:'components', replacement:path.resolve(projectRootDir, "src/components")}
				]
		}),
		replace({
			'process.env.SERVER_HOST': JSON.stringify( process.env.SERVER_HOST ),
			'process.env.SERVER_PORT': JSON.stringify( process.env.SERVER_PORT ),
		}),
		svelte({
			preprocess: preprocess({
				replace: [["process.env.PATH", process.env.PATH]],
			}),
			compilerOptions: {
				dev: !production
			}
		}),
		css({ output: 'bundle.css' }),
		resolve({
			browser: true,
			dedupe: ['svelte']
		}),
		commonjs(),
		json(),
		!production && serve(),
		!production && livereload('public'),
		production && terser()
	],
	watch: {
		chokidar: {
			usePolling: true
		},
		clearScreen: false
	}
};
