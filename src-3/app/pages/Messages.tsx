import { useState } from "react";
import { Link, useSearchParams } from "react-router";
import { ArrowLeft, Send } from "lucide-react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { BottomNav } from "../components/BottomNav";

interface Message {
  id: number;
  text: string;
  sender: "trainer" | "client";
  timestamp: string;
}

const mockMessages: Message[] = [
  {
    id: 1,
    text: "Hey! How was your workout today?",
    sender: "trainer",
    timestamp: "10:30 AM",
  },
  {
    id: 2,
    text: "It was great! Managed to hit all my targets.",
    sender: "client",
    timestamp: "10:45 AM",
  },
  {
    id: 3,
    text: "Awesome! Keep up the good work. See you tomorrow.",
    sender: "trainer",
    timestamp: "11:00 AM",
  },
];

export function Messages() {
  const [searchParams] = useSearchParams();
  const clientId = searchParams.get("client");
  const [messages, setMessages] = useState<Message[]>(mockMessages);
  const [newMessage, setNewMessage] = useState("");

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    const message: Message = {
      id: Date.now(),
      text: newMessage,
      sender: "trainer",
      timestamp: new Date().toLocaleTimeString("en-US", {
        hour: "numeric",
        minute: "2-digit",
      }),
    };

    setMessages([...messages, message]);
    setNewMessage("");
  };

  return (
    <>
      <div className="min-h-screen p-6 pb-24 flex flex-col">
        <div className="max-w-4xl mx-auto w-full flex-1 flex flex-col space-y-6">
          <div className="flex items-center gap-4">
            <Link to="/clients">
              <button className="text-white hover:text-primary">
                <ArrowLeft className="w-6 h-6" />
              </button>
            </Link>
            <div>
              <h1 className="text-white">Messages</h1>
              <p className="text-muted-foreground text-sm">Client #{clientId}</p>
            </div>
          </div>

          <Card className="bg-card border-border flex-1 flex flex-col">
            <div className="flex-1 p-6 space-y-4 overflow-y-auto">
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={`flex ${
                    message.sender === "trainer" ? "justify-end" : "justify-start"
                  }`}
                >
                  <div
                    className={`max-w-[70%] rounded-lg p-3 ${
                      message.sender === "trainer"
                        ? "bg-primary text-primary-foreground"
                        : "bg-background border border-border text-white"
                    }`}
                  >
                    <p>{message.text}</p>
                    <p
                      className={`text-xs mt-1 ${
                        message.sender === "trainer"
                          ? "text-primary-foreground/70"
                          : "text-muted-foreground"
                      }`}
                    >
                      {message.timestamp}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            <form onSubmit={handleSend} className="p-4 border-t border-border">
              <div className="flex gap-3">
                <Input
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  placeholder="Type a message..."
                  className="bg-input-background border-border text-white"
                />
                <Button
                  type="submit"
                  className="bg-primary hover:bg-primary/90 text-primary-foreground"
                >
                  <Send className="w-4 h-4" />
                </Button>
              </div>
            </form>
          </Card>
        </div>
      </div>
      <BottomNav />
    </>
  );
}
