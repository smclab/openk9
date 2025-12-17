import { useKeycloak } from "../config/auth";
import { keycloak as realKeycloak } from "../components/keycloak";

type KC = {
	authenticated: boolean;
	token?: string;
	init(opts: any): Promise<boolean>;
	updateToken(minValidity: number): Promise<boolean>;
	login(): Promise<void>;
	logout(): Promise<void>;
	loadUserInfo(): Promise<any>;
};

const noAuthKeycloak: KC = {
	authenticated: false,
	token: undefined,
	async init() {
		return false;
	},
	async updateToken() {
		return false;
	},
	async login() {},
	async logout() {},
	async loadUserInfo() {
		return null;
	},
};

export const kc: KC = useKeycloak ? (realKeycloak as KC) : noAuthKeycloak;
