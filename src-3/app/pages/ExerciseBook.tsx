import { Link } from "react-router";
import { ArrowLeft, Plus } from "lucide-react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { BottomNav } from "../components/BottomNav";
import { useState, useEffect } from "react";

const defaultExercises = [
  { id: 1, name: "Bench Press", sets: 3, reps: 10, weight: 135, isCustom: false },
  { id: 2, name: "Barbell Squat", sets: 4, reps: 8, weight: 185, isCustom: false },
  { id: 3, name: "Deadlift", sets: 3, reps: 5, weight: 225, isCustom: false },
  { id: 4, name: "Hack Squat", sets: 3, reps: 10, weight: 180, isCustom: false },
  { id: 5, name: "Leg Press", sets: 3, reps: 12, weight: 300, isCustom: false },
  { id: 6, name: "Leg Extension", sets: 3, reps: 12, weight: 90, isCustom: false },
  { id: 7, name: "Leg Curl", sets: 3, reps: 12, weight: 70, isCustom: false },
  { id: 8, name: "Hip Thrust", sets: 3, reps: 12, weight: 135, isCustom: false },
  { id: 9, name: "Abductors", sets: 3, reps: 15, weight: 80, isCustom: false },
  { id: 10, name: "Adductors", sets: 3, reps: 15, weight: 80, isCustom: false },
  { id: 11, name: "Calf Raises", sets: 4, reps: 15, weight: 90, isCustom: false },
  { id: 12, name: "Barbell Row", sets: 3, reps: 10, weight: 135, isCustom: false },
  { id: 13, name: "T-Bar Row", sets: 3, reps: 10, weight: 90, isCustom: false },
  { id: 14, name: "Seated Rows", sets: 3, reps: 12, weight: 120, isCustom: false },
  { id: 15, name: "Seated Cable Rows", sets: 3, reps: 12, weight: 120, isCustom: false },
  { id: 16, name: "Lat Pulldown", sets: 3, reps: 10, weight: 100, isCustom: false },
  { id: 17, name: "Tricep Pushdowns", sets: 3, reps: 12, weight: 60, isCustom: false },
  { id: 18, name: "Pull Ups", sets: 3, reps: 8, weight: 0, isCustom: false },
  { id: 19, name: "Dips", sets: 3, reps: 10, weight: 0, isCustom: false },
  { id: 20, name: "Lat Raises", sets: 3, reps: 12, weight: 20, isCustom: false },
  { id: 21, name: "RDLs", sets: 3, reps: 10, weight: 135, isCustom: false },
  { id: 22, name: "SLDLs", sets: 3, reps: 10, weight: 135, isCustom: false },
  { id: 23, name: "JM Press", sets: 3, reps: 10, weight: 95, isCustom: false },
  { id: 24, name: "Incline Dumbbell Press", sets: 3, reps: 10, weight: 60, isCustom: false },
  { id: 25, name: "Pec Flys", sets: 3, reps: 12, weight: 40, isCustom: false },
  { id: 26, name: "Preacher Curls", sets: 3, reps: 12, weight: 50, isCustom: false },
  { id: 27, name: "Crunches", sets: 3, reps: 20, weight: 0, isCustom: false },
];

export function ExerciseBook() {
  const [exercises, setExercises] = useState(defaultExercises);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newExercise, setNewExercise] = useState({ name: "" });

  useEffect(() => {
    const savedExercises = localStorage.getItem("customExercises");
    if (savedExercises) {
      const custom = JSON.parse(savedExercises);
      setExercises([...defaultExercises, ...custom]);
    }
  }, []);

  const handleAddExercise = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newExercise.name.trim()) return;

    const customExercise = {
      id: Date.now(),
      name: newExercise.name,
      sets: 3,
      reps: 10,
      weight: 0,
      isCustom: true,
    };

    const customExercises = exercises.filter(ex => ex.isCustom);
    customExercises.push(customExercise);
    
    localStorage.setItem("customExercises", JSON.stringify(customExercises));
    setExercises([...exercises, customExercise]);
    setNewExercise({ name: "" });
    setShowAddForm(false);
  };

  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-4xl mx-auto space-y-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Link to="/activity">
                <button className="text-white hover:text-primary">
                  <ArrowLeft className="w-6 h-6" />
                </button>
              </Link>
              <h1 className="text-white">Exercise Book</h1>
            </div>
            <Button
              size="sm"
              onClick={() => setShowAddForm(!showAddForm)}
              className="bg-primary hover:bg-primary/90 text-primary-foreground"
            >
              <Plus className="w-4 h-4 mr-2" />
              Add Exercise
            </Button>
          </div>

          {showAddForm && (
            <Card className="bg-card border-border p-6">
              <form onSubmit={handleAddExercise} className="space-y-4">
                <h3 className="text-white">Add Custom Exercise</h3>
                <input
                  type="text"
                  value={newExercise.name}
                  onChange={(e) => setNewExercise({ name: e.target.value })}
                  placeholder="Exercise name"
                  className="w-full bg-input-background border border-border rounded-lg px-4 py-2 text-white"
                  required
                />
                <div className="flex gap-3">
                  <Button
                    type="submit"
                    className="bg-primary hover:bg-primary/90 text-primary-foreground"
                  >
                    Add
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => setShowAddForm(false)}
                    className="border-border text-white hover:bg-card"
                  >
                    Cancel
                  </Button>
                </div>
              </form>
            </Card>
          )}

          <div className="space-y-3">
            {exercises.map((exercise) => (
              <Link key={exercise.id} to={`/exercise-log?exercise=${encodeURIComponent(exercise.name)}`}>
                <Card className="bg-card border-border p-4 hover:bg-card/80 transition-colors cursor-pointer">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <h3 className="text-white mb-1">{exercise.name}</h3>
                        {exercise.isCustom && (
                          <span className="text-xs bg-primary/20 text-primary px-2 py-1 rounded">
                            Custom
                          </span>
                        )}
                      </div>
                      <div className="flex gap-4">
                        <span className="text-muted-foreground">
                          Sets: <span className="text-white">{exercise.sets}</span>
                        </span>
                        <span className="text-muted-foreground">
                          Reps: <span className="text-white">{exercise.reps}</span>
                        </span>
                        {exercise.weight > 0 && (
                          <span className="text-muted-foreground">
                            Weight: <span className="text-white">{exercise.weight} lbs</span>
                          </span>
                        )}
                      </div>
                    </div>
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