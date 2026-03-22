import { Link, useSearchParams } from "react-router";
import { ArrowLeft, Calendar } from "lucide-react";
import { Card } from "../components/ui/card";
import { BottomNav } from "../components/BottomNav";
import { useState, useEffect } from "react";

// Mock data for cardio workouts
const mockCardioHistory = [
  {
    id: 1,
    activity: "Running",
    date: "March 19, 2026",
    time: "45 min",
    distance: "5.2 km",
    pace: "5:30 /km",
    calories: 500,
    heartRate: 150,
  },
  {
    id: 2,
    activity: "Running",
    date: "March 15, 2026",
    time: "40 min",
    distance: "4.8 km",
    pace: "5:45 /km",
    calories: 450,
    heartRate: 145,
  },
  {
    id: 3,
    activity: "Cycling",
    date: "March 12, 2026",
    time: "60 min",
    distance: "25 km",
    speed: "25 km/h",
    calories: 600,
    heartRate: 140,
  },
];

export function WorkoutHistory() {
  const [searchParams] = useSearchParams();
  const type = searchParams.get("type") || "weightlifting";
  const exerciseName = searchParams.get("exercise");
  const [weightliftingHistory, setWeightliftingHistory] = useState<any[]>([]);

  const isWeightlifting = type === "weightlifting";
  
  useEffect(() => {
    // Load workouts from localStorage
    const savedWorkouts = localStorage.getItem("workoutHistory");
    if (savedWorkouts) {
      setWeightliftingHistory(JSON.parse(savedWorkouts));
    }
  }, []);

  // Filter weightlifting history by specific exercise if provided
  const filteredWeightliftingHistory = exerciseName 
    ? weightliftingHistory.filter(workout => workout.exercise === exerciseName)
    : weightliftingHistory;
  
  const history = isWeightlifting ? filteredWeightliftingHistory : mockCardioHistory;

  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-4xl mx-auto space-y-6">
          <div className="flex items-center gap-4">
            <Link to={exerciseName ? `/exercise-log?exercise=${encodeURIComponent(exerciseName)}` : (isWeightlifting ? "/exercises" : "/activity")}>
              <button className="text-white hover:text-primary">
                <ArrowLeft className="w-6 h-6" />
              </button>
            </Link>
            <div>
              <h1 className="text-white">
                {exerciseName ? `${exerciseName} History` : "Workout History"}
              </h1>
              {exerciseName && (
                <p className="text-muted-foreground text-sm">
                  All previous {exerciseName.toLowerCase()} sessions
                </p>
              )}
            </div>
          </div>

          {history.length === 0 ? (
            <Card className="bg-card border-border p-12">
              <div className="text-center space-y-2">
                <p className="text-white">No workout history yet</p>
                <p className="text-muted-foreground">
                  Complete your first workout to see it here
                </p>
              </div>
            </Card>
          ) : (
            <div className="space-y-4">
              {isWeightlifting ? (
                // Weightlifting history
                filteredWeightliftingHistory.map((workout) => (
                  <Card key={workout.id} className="bg-card border-border p-6">
                    <div className="space-y-4">
                      <div className="flex items-center justify-between">
                        {!exerciseName && <h3 className="text-white">{workout.exercise}</h3>}
                        <div className="flex items-center gap-2 text-muted-foreground">
                          <Calendar className="w-4 h-4" />
                          <span>{workout.date}</span>
                        </div>
                      </div>

                      <div className="space-y-2">
                        {workout.sets.map((set, index) => (
                          <div
                            key={index}
                            className="flex items-center justify-between bg-background rounded-lg p-3 border border-border"
                          >
                            <span className="text-white">Set {index + 1}</span>
                            <div className="flex gap-6">
                              <span className="text-muted-foreground">
                                Reps: <span className="text-white">{set.reps}</span>
                              </span>
                              <span className="text-muted-foreground">
                                Weight: <span className="text-white">{set.weight} lbs</span>
                              </span>
                            </div>
                          </div>
                        ))}
                      </div>

                      <div className="pt-2 border-t border-border">
                        <div className="text-white">
                          Total Volume:{" "}
                          <span className="text-primary">
                            {workout.sets.reduce(
                              (acc, set) => acc + set.reps * set.weight,
                              0
                            ).toLocaleString()}{" "}
                            lbs
                          </span>
                        </div>
                      </div>
                    </div>
                  </Card>
                ))
              ) : (
                // Cardio history
                mockCardioHistory.map((workout) => (
                  <Card key={workout.id} className="bg-card border-border p-6">
                    <div className="space-y-4">
                      <div className="flex items-center justify-between">
                        <h3 className="text-white">{workout.activity}</h3>
                        <div className="flex items-center gap-2 text-muted-foreground">
                          <Calendar className="w-4 h-4" />
                          <span>{workout.date}</span>
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-4">
                        <div className="bg-background rounded-lg p-3 border border-border">
                          <p className="text-muted-foreground text-sm">Time</p>
                          <p className="text-white">{workout.time}</p>
                        </div>
                        <div className="bg-background rounded-lg p-3 border border-border">
                          <p className="text-muted-foreground text-sm">Distance</p>
                          <p className="text-white">{workout.distance}</p>
                        </div>
                        {"pace" in workout && (
                          <div className="bg-background rounded-lg p-3 border border-border">
                            <p className="text-muted-foreground text-sm">Pace</p>
                            <p className="text-white">{workout.pace}</p>
                          </div>
                        )}
                        {"speed" in workout && (
                          <div className="bg-background rounded-lg p-3 border border-border">
                            <p className="text-muted-foreground text-sm">Speed</p>
                            <p className="text-white">{workout.speed}</p>
                          </div>
                        )}
                        <div className="bg-background rounded-lg p-3 border border-border">
                          <p className="text-muted-foreground text-sm">Calories</p>
                          <p className="text-white">{workout.calories} cal</p>
                        </div>
                        <div className="bg-background rounded-lg p-3 border border-border">
                          <p className="text-muted-foreground text-sm">Heart Rate</p>
                          <p className="text-white">{workout.heartRate} bpm</p>
                        </div>
                      </div>
                    </div>
                  </Card>
                ))
              )}
            </div>
          )}
        </div>
      </div>
      <BottomNav />
    </>
  );
}