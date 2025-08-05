"use client";
import { useState } from 'react';
import { X } from 'lucide-react';
import { useAuth } from '../utils/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { toast } from 'sonner';

export default function SignupPopup({ onClose }) {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    passwordConfirmation: ''
  });

  const { signup } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (formData.password !== formData.passwordConfirmation) {
        throw new Error('Passwords do not match');
      }
      await signup(formData);
      toast.success('Signup successful! Please login');
      onClose();
    } catch (error) {
      toast.error('Signup failed', { description: error.message });
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center from-black/80 via-gray-900/80 to-gray-800/80 backdrop-blur-xl">
      <Card className="relative w-full max-w-md from-gray-900 via-black to-gray-900 text-white border border-gray-800">
        <Button
          variant="ghost"
          size="icon"
          className="absolute right-4 top-4 h-8 w-8 text-gray-400 hover:bg-gray-800/50"
          onClick={onClose}
        >
          <X className="h-4 w-4" />
        </Button>

        <CardHeader>
          <CardTitle className="text-3xl font-bold text-gray-100">Create Account</CardTitle>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName" className="text-white">First Name</Label>
                <Input
                  id="firstName"
                  value={formData.firstName}
                  onChange={(e) => setFormData({...formData, firstName: e.target.value})}
                  className="bg-white/10 border-white/20 text-white placeholder:text-white/70"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName" className="text-white">Last Name</Label>
                <Input
                  id="lastName"
                  value={formData.lastName}
                  onChange={(e) => setFormData({...formData, lastName: e.target.value})}
                  className="bg-white/10 border-white/20 text-white placeholder:text-white/70"
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="username" className="text-white">Username</Label>
              <Input
                id="username"
                value={formData.username}
                onChange={(e) => setFormData({...formData, username: e.target.value})}
                className="bg-white/10 border-white/20 text-white placeholder:text-white/70"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email" className="text-white">Email</Label>
              <Input
                id="email"
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({...formData, email: e.target.value})}
                className="bg-white/10 border-white/20 text-white placeholder:text-white/70"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-white">Password</Label>
              <Input
                id="password"
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({...formData, password: e.target.value})}
                className="bg-white/10 border-white/20 text-white placeholder:text-white/70"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="passwordConfirmation" className="text-white">Confirm Password</Label>
              <Input
                id="passwordConfirmation"
                type="password"
                value={formData.passwordConfirmation}
                onChange={(e) => setFormData({...formData, passwordConfirmation: e.target.value})}
                className="bg-white/10 border-white/20 text-white placeholder:text-white/70"
                required
              />
            </div>

            <Button 
              type="submit" 
              className="w-full mt-6 py-6 text-lg font-semibold bg-gray-100 text-gray-900 hover:bg-gray-200 transition-colors duration-200"
            >
              Sign Up
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}