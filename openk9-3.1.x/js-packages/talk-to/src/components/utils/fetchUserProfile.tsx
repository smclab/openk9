import { getUserProfile } from "../authentication";

export type chatId = {
	id: string | null;
	isNew: boolean;
} | null;

export default async function fetchUserProfile({
	setChatId,
	setUserId,
}: {
	setChatId: React.Dispatch<React.SetStateAction<chatId>>;
	setUserId: React.Dispatch<React.SetStateAction<string | undefined | null>>;
}) {
	try {
		const profile: { sub: string } = await getUserProfile();
		if (profile.sub) {
			const userId = profile.sub + "_" + String(Date.now());
			setUserId(profile.sub);
			setChatId({ id: userId, isNew: true });
		} else {
			const userId = String(Date.now());
			setChatId({ id: userId, isNew: true });
		}
	} catch (error) {
		const userId = String(Date.now());
		setChatId({ id: userId, isNew: true });
	}
}
