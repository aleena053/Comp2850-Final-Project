import { createBrowserRouter } from "react-router";
import { Home } from "./pages/Home";
import { Login } from "./pages/Login";
import { SignUp } from "./pages/SignUp";
import { Dashboard } from "./pages/Dashboard";
import { ActivitySelect } from "./pages/ActivitySelect";
import { ExerciseBook } from "./pages/ExerciseBook";
import { ExerciseLog } from "./pages/ExerciseLog";
import { WorkoutLog } from "./pages/WorkoutLog";
import { WorkoutHistory } from "./pages/WorkoutHistory";
import { Profile } from "./pages/Profile";
import { Progress } from "./pages/Progress";
import { Calendar } from "./pages/Calendar";
import { Clients } from "./pages/Clients";
import { Messages } from "./pages/Messages";
import { ScheduleWorkout } from "./pages/ScheduleWorkout";
import { NotFound } from "./pages/NotFound";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: Home,
  },
  {
    path: "/login",
    Component: Login,
  },
  {
    path: "/signup",
    Component: SignUp,
  },
  {
    path: "/dashboard",
    Component: Dashboard,
  },
  {
    path: "/activity",
    Component: ActivitySelect,
  },
  {
    path: "/exercises",
    Component: ExerciseBook,
  },
  {
    path: "/exercise-log",
    Component: ExerciseLog,
  },
  {
    path: "/workout",
    Component: WorkoutLog,
  },
  {
    path: "/history",
    Component: WorkoutHistory,
  },
  {
    path: "/profile",
    Component: Profile,
  },
  {
    path: "/progress",
    Component: Progress,
  },
  {
    path: "/calendar",
    Component: Calendar,
  },
  {
    path: "/clients",
    Component: Clients,
  },
  {
    path: "/messages",
    Component: Messages,
  },
  {
    path: "/schedule-workout",
    Component: ScheduleWorkout,
  },
  {
    path: "*",
    Component: NotFound,
  },
]);