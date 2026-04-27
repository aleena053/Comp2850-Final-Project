import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { ArrowLeft } from "lucide-react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { BottomNav } from "../components/BottomNav";

export function WorkoutLog() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const activity = searchParams.get("activity") || "running";
  
  const [workoutData, setWorkoutData] = useState({
    time: "",
    target: "",
    distance: "",
    targetRate: "",
    heartRate: "",
    elevation: "",
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Save workout and show map
    navigate("/dashboard");
  };

  // Get activity-specific fields
  const getActivityFields = () => {
    switch (activity) {
      case "running":
        return {
          title: "Running Workout",
          fields: [
            { id: "time", label: "Time", placeholder: "45 min" },
            { id: "target", label: "Target", placeholder: "500 cal" },
            { id: "distance", label: "Distance", placeholder: "5.2 km" },
            { id: "targetRate", label: "Target Pace", placeholder: "5:30 /km" },
            { id: "heartRate", label: "Heart Rate", placeholder: "150 bpm" },
          ],
        };
      case "cycling":
        return {
          title: "Cycling Workout",
          fields: [
            { id: "time", label: "Time", placeholder: "60 min" },
            { id: "target", label: "Target", placeholder: "600 cal" },
            { id: "distance", label: "Distance", placeholder: "25 km" },
            { id: "targetRate", label: "Target Speed", placeholder: "25 km/h" },
            { id: "heartRate", label: "Heart Rate", placeholder: "140 bpm" },
          ],
        };
      case "hiking":
        return {
          title: "Hiking Workout",
          fields: [
            { id: "time", label: "Time", placeholder: "120 min" },
            { id: "target", label: "Target", placeholder: "400 cal" },
            { id: "distance", label: "Distance", placeholder: "8 km" },
            { id: "elevation", label: "Elevation", placeholder: "450 m" },
            { id: "heartRate", label: "Heart Rate", placeholder: "130 bpm" },
          ],
        };
      case "swimming":
        return {
          title: "Swimming Workout",
          fields: [
            { id: "time", label: "Time", placeholder: "30 min" },
            { id: "target", label: "Target", placeholder: "400 cal" },
            { id: "distance", label: "Distance", placeholder: "1500 m" },
            { id: "targetRate", label: "Target Pace", placeholder: "2:00 /100m" },
            { id: "heartRate", label: "Heart Rate", placeholder: "145 bpm" },
          ],
        };
      case "yoga":
        return {
          title: "Yoga Session",
          fields: [
            { id: "time", label: "Time", placeholder: "45 min" },
            { id: "target", label: "Target", placeholder: "200 cal" },
            { id: "heartRate", label: "Heart Rate", placeholder: "110 bpm" },
          ],
        };
      default:
        return {
          title: "Workout",
          fields: [
            { id: "time", label: "Time", placeholder: "45 min" },
            { id: "target", label: "Target", placeholder: "500 cal" },
            { id: "distance", label: "Distance", placeholder: "5 km" },
            { id: "heartRate", label: "Heart Rate", placeholder: "150 bpm" },
          ],
        };
    }
  };

  const activityConfig = getActivityFields();

  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-2xl mx-auto space-y-6">
          <div className="flex items-center gap-4">
            <Link to="/activity">
              <button className="text-white hover:text-primary">
                <ArrowLeft className="w-6 h-6" />
              </button>
            </Link>
            <h1 className="text-white">{activityConfig.title}</h1>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <Card className="bg-card border-border p-6">
              <div className="space-y-4">
                <h3 className="text-white mb-4">Workout Stats</h3>
                
                <div className="grid grid-cols-2 gap-4">
                  {activityConfig.fields.map((field) => (
                    <div 
                      key={field.id} 
                      className={field.id === "heartRate" ? "col-span-2" : ""}
                    >
                      <Label htmlFor={field.id} className="text-white">
                        {field.label}
                      </Label>
                      <Input
                        id={field.id}
                        type="text"
                        value={workoutData[field.id as keyof typeof workoutData]}
                        onChange={(e) =>
                          setWorkoutData({ ...workoutData, [field.id]: e.target.value })
                        }
                        className="bg-input-background border-border text-white mt-2"
                        placeholder={field.placeholder}
                      />
                    </div>
                  ))}
                </div>
              </div>
            </Card>

            <Card className="bg-card border-border p-6">
              <div className="space-y-4">
                <h3 className="text-white">Map of Movement</h3>
                <div className="aspect-video bg-background rounded-lg flex items-center justify-center border border-border">
                  <p className="text-muted-foreground">Map tracking would appear here</p>
                </div>
                <p className="text-muted-foreground text-center">
                  GPS tracking shows your workout path
                </p>
              </div>
            </Card>

            <Button 
              type="submit" 
              className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
            >
              Save Workout
            </Button>
          </form>
        </div>
      </div>
      <BottomNav />
    </>
  );
}