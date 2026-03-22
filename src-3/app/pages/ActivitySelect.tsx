import { Link } from "react-router";
import { ArrowLeft, Dumbbell, Bike, Mountain, HeartPulse, Waves, Flower2 } from "lucide-react";
import { Card } from "../components/ui/card";
import { BottomNav } from "../components/BottomNav";

const activities = [
  { id: 1, name: "Running", icon: HeartPulse, color: "bg-primary", type: "cardio" },
  { id: 2, name: "Cycling", icon: Bike, color: "bg-primary", type: "cardio" },
  { id: 3, name: "Weightlifting", icon: Dumbbell, color: "bg-primary", type: "strength" },
  { id: 4, name: "Hiking", icon: Mountain, color: "bg-primary", type: "cardio" },
  { id: 5, name: "Swimming", icon: Waves, color: "bg-primary", type: "cardio" },
  { id: 6, name: "Yoga", icon: Flower2, color: "bg-primary", type: "flexibility" },
];

export function ActivitySelect() {
  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-4xl mx-auto space-y-6">
          <div className="flex items-center gap-4">
            <Link to="/dashboard">
              <button className="text-white hover:text-primary">
                <ArrowLeft className="w-6 h-6" />
              </button>
            </Link>
            <h1 className="text-white">Select Activity</h1>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {activities.map((activity) => (
              <Link key={activity.id} to={activity.type === "strength" ? "/exercises" : `/workout?activity=${activity.name.toLowerCase()}`}>
                <Card className="bg-card border-border p-8 hover:bg-card/80 transition-colors cursor-pointer">
                  <div className="flex flex-col items-center justify-center space-y-4">
                    <div className={`w-16 h-16 rounded-full ${activity.color} flex items-center justify-center`}>
                      <activity.icon className="w-8 h-8 text-primary-foreground" />
                    </div>
                    <p className="text-white text-center">{activity.name}</p>
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        </div>
      </div>
      <BottomNav />
    </>
  );
}