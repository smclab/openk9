import create from "zustand";
import { devtools, persist } from "zustand/middleware";

const persistVersion = "v1";

export type StateType = { sidebarOpen: boolean; toggleSidebar(): void };

export const useStore = create<StateType>(
  persist(
    devtools((set, get) => ({
      sidebarOpen: true,
      toggleSidebar() {
        set((state) => ({ ...state, sidebarOpen: !state.sidebarOpen }));
      },
    })),
    { name: "pq-admin-" + persistVersion },
  ),
);
