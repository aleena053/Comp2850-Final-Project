import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { ArrowLeft, Plus, Trash2 } from "lucide-react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { BottomNav } from "../components/BottomNav";

interface ExerciseSet {
  id: string;
  reps: string;
  weight: string;
}

export function ExerciseLog() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const exerciseName = searchParams.get("exercise") || "Exercise";
  
  const [sets, setSets] = useState<ExerciseSet[]>([
    { id: "1", reps: "", weight: "" },
  ]);

  const addSet = () => {
    const newSet: ExerciseSet = {
      id: Date.now().toString(),
      reps: "",
      weight: "",
    };
    setSets([...sets, newSet]);
  };

  const removeSet = (id: string) => {
    if (sets.length > 1) {
      setSets(sets.filter((set) => set.id !== id));
    }
  };

  const updateSet = (id: string, field: "reps" | "weight", value: string) => {
    setSets(
      sets.map((set) =>
        set.id === id ? { ...set, [field]: value } : set
      )
    );
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Save workout to history
    const workout = {
      id: Date.now(),
      exercise: exerciseName,
      date: new Date().toLocaleDateString("en-US", { 
        year: "numeric", 
        month: "long", 
        day: "numeric" 
      }),
      sets: sets.map(set => ({
        reps: parseInt(set.reps) || 0,
        weight: parseInt(set.weight) || 0,
      })),
    };

    // Get existing workouts
    const existingWorkouts = localStorage.getItem("workoutHistory");
    const workouts = existingWorkouts ? JSON.parse(existingWorkouts) : [];
    
    // Add new workout
    workouts.unshift(workout); // Add to beginning of array
    
    // Save back to localStorage
    localStorage.setItem("workoutHistory", JSON.stringify(workouts));
    
    // Navigate back to exercises
    navigate("/exercises");
  };

  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-2xl mx-auto space-y-6">
          <div className="flex items-center gap-4">
            <Link to="/exercises">
              <button className="text-white hover:text-primary">
                <ArrowLeft className="w-6 h-6" />
              </button>
            </Link>
            <h1 className="text-white">{exerciseName}</h1>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <Card className="bg-card border-border p-6">
              <div className="space-y-4">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-white">Sets</h3>
                  <Button
                    type="button"
                    size="sm"
                    onClick={addSet}
                    className="bg-primary hover:bg-primary/90 text-primary-foreground"
                  >
                    <Plus className="w-4 h-4 mr-2" />
                    Add Set
                  </Button>
                </div>

                <div className="space-y-3">
                  {sets.map((set, index) => (
                    <div
                      key={set.id}
                      className="bg-background rounded-lg p-4 border border-border"
                    >
                      <div className="flex items-center gap-4">
                        <div className="text-white font-medium w-12">
                          Set {index + 1}
                        </div>
                        <div className="flex-1 grid grid-cols-2 gap-4">
                          <div>
                            <Label htmlFor={`reps-${set.id}`} className="text-white text-sm">
                              Reps
                            </Label>
                            <Input
                              id={`reps-${set.id}`}
                              type="number"
                              value={set.reps}
                              onChange={(e) => updateSet(set.id, "reps", e.target.value)}
                              className="bg-input-background border-border text-white mt-1"
                              placeholder="10"
                            />
                          </div>
                          <div>
                            <Label htmlFor={`weight-${set.id}`} className="text-white text-sm">
                              Weight (lbs)
                            </Label>
                            <Input
                              id={`weight-${set.id}`}
                              type="number"
                              value={set.weight}
                              onChange={(e) => updateSet(set.id, "weight", e.target.value)}
                              className="bg-input-background border-border text-white mt-1"
                              placeholder="135"
                            />
                          </div>
                        </div>
                        {sets.length > 1 && (
                          <button
                            type="button"
                            onClick={() => removeSet(set.id)}
                            className="text-red-500 hover:text-red-400 p-2"
                          >
                            <Trash2 className="w-5 h-5" />
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </Card>

            <div className="flex gap-4">
              <Link to={`/history?type=weightlifting&exercise=${encodeURIComponent(exerciseName)}`} className="flex-1">
                <Button
                  type="button"
                  variant="outline"
                  className="w-full border-border text-white hover:bg-card"
                >
                  View History
                </Button>
              </Link>
              <Button
                type="submit"
                className="flex-1 bg-primary hover:bg-primary/90 text-primary-foreground"
              >
                Save Workout
              </Button>
            </div>
          </form>
        </div>
      </div>
      <BottomNav />
    </>
  );
}